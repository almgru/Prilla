package com.almgru.trabacco.controller;

import com.almgru.trabacco.data.EntryRepository;
import com.almgru.trabacco.dto.DurationDataDTO;
import com.almgru.trabacco.dto.DataRequestDTO;
import com.almgru.trabacco.dto.TimeSeriesDataDTO;
import com.almgru.trabacco.service.DateIterator;
import com.almgru.trabacco.service.EntryConverter;
import com.almgru.trabacco.service.EntryGrouper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
public class APIController {
    private final EntryRepository repository;
    private final EntryConverter entryConverter;
    private final EntryGrouper entryGrouper;

    @Autowired
    public APIController(EntryRepository repository, EntryConverter entryConverter, EntryGrouper entryGrouper) {
        this.repository = repository;
        this.entryConverter = entryConverter;
        this.entryGrouper = entryGrouper;
    }

    @GetMapping("amount-data")
    public List<TimeSeriesDataDTO> amountData(@Valid @ModelAttribute DataRequestDTO request, Locale locale) {
        LocalDate startDate;
        LocalDate endDate;

        switch (request.span()) {
            case WEEK -> {
                var weeksInYear = IsoFields.WEEK_OF_WEEK_BASED_YEAR
                        .rangeRefinedBy(LocalDate.of(request.year(), 1, 1)).getMaximum();

                if (request.week() > weeksInYear) {
                    throw new IllegalArgumentException(
                            String.format("No week %d in year %s.\n", request.week(), request.year()));
                }

                startDate = LocalDate.of(request.year(), 6, 1)
                        .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, request.week())
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                endDate = startDate.plusWeeks(1).minusDays(1);

                return entryGrouper.groupAmount(
                        repository.findByAppliedDateIsBetween(startDate, endDate),
                        entry -> entry.getAppliedAt().toLocalDate(),
                        new DateIterator(startDate, endDate, ChronoUnit.DAYS)
                    )
                    .entrySet()
                    .stream()
                    .map(e -> entryConverter.keyValuePairToTimeSeriesDTO(e, DateTimeFormatter.ISO_DATE::format))
                    .collect(Collectors.toList());
            }
            case MONTH -> {
                startDate = LocalDate.of(request.year(), request.month(), 1);
                endDate = startDate.plusMonths(1).minusDays(1);
                var weekFields = WeekFields.of(locale);

                return entryGrouper.groupAmount(
                            repository.findByAppliedDateIsBetween(startDate, endDate),
                            entry -> entry.getAppliedAt().toLocalDate().get(weekFields.weekOfWeekBasedYear()),
                            date -> date.get(weekFields.weekOfWeekBasedYear()),
                            new DateIterator(startDate, endDate, ChronoUnit.WEEKS)
                        )
                        .entrySet()
                        .stream()
                        .map(e -> entryConverter.keyValuePairToTimeSeriesDTO(e, i -> Integer.toString(i)))
                        .collect(Collectors.toList());
            }
            case YEAR -> {
                startDate = LocalDate.of(request.year(), 1, 1);
                endDate = startDate.plusYears(1).minusDays(1);

                return entryGrouper.groupAmount(
                        repository.findByAppliedDateIsBetween(startDate, endDate),
                        entry -> entry.getAppliedAt().toLocalDate(),
                        new DateIterator(startDate, endDate, ChronoUnit.WEEKS)
                    )
                    .entrySet()
                    .stream()
                    .map(e -> entryConverter.keyValuePairToTimeSeriesDTO(e, DateTimeFormatter.ofPattern("MMM")::format))
                    .collect(Collectors.toList());
            }
            default -> throw new IllegalStateException();
        }
    }

    @GetMapping("duration-data")
    public List<DurationDataDTO> durationData(@Valid @ModelAttribute DataRequestDTO request) {
        var weeksInYear = IsoFields.WEEK_OF_WEEK_BASED_YEAR
                .rangeRefinedBy(LocalDate.of(request.year(), 1, 1)).getMaximum();

        if (request.week() > weeksInYear) {
            throw new IllegalArgumentException(
                    String.format("No week %d in year %s.\n", request.week(), request.year()));
        }

        var firstDayOfWeek = LocalDate.of(request.year(), 6, 1)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, request.week())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        var lastDayOfWeek = firstDayOfWeek.plusDays(6);

        var durationsGroupedByDate = repository
                .findByAppliedDateIsBetween(firstDayOfWeek, lastDayOfWeek)
                .stream()
                .collect(
                        Collectors.groupingBy(
                                entry -> entry.getAppliedAt().toLocalDate(),
                                Collectors.mapping(
                                        entry -> Duration.between(entry.getAppliedAt(), entry.getRemovedAt()).toMinutes(),
                                        Collectors.toList())));

        return Stream
                .iterate(firstDayOfWeek, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(firstDayOfWeek, lastDayOfWeek) + 1)
                .map(date -> new DurationDataDTO(DateTimeFormatter.ISO_DATE.format(date),
                        durationsGroupedByDate.getOrDefault(date, Collections.emptyList())))
                .collect(Collectors.toList());
    }
}

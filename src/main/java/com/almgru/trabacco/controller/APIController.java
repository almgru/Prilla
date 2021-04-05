package com.almgru.trabacco.controller;

import com.almgru.trabacco.data.EntryRepository;
import com.almgru.trabacco.dto.DurationDataDTO;
import com.almgru.trabacco.dto.DataRequestDTO;
import com.almgru.trabacco.dto.TimeSeriesDataDTO;
import com.almgru.trabacco.entity.Entry;
import com.almgru.trabacco.enums.TimeSpan;
import com.almgru.trabacco.service.TextFormatter;
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
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.time.temporal.WeekFields;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
public class APIController {
    private final EntryRepository repository;
    private final TextFormatter textFormatter;

    @Autowired
    public APIController(EntryRepository repository, TextFormatter textFormatter) {
        this.repository = repository;
        this.textFormatter = textFormatter;
    }

    @GetMapping("amount-data")
    public List<TimeSeriesDataDTO> amountData(@Valid @ModelAttribute DataRequestDTO request, Locale locale) {
        var weekFields = WeekFields.of(locale);
        LocalDate startDate;
        LocalDate endDate;
        long between;
        TemporalUnit unit;
        Function<Entry, String> entryToKey;
        Function<TemporalAccessor, String> dateToKey;

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
                between = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                unit = ChronoUnit.DAYS;
                dateToKey = DateTimeFormatter.ISO_DATE::format;
                entryToKey = entry -> dateToKey.apply(entry.getAppliedAt().toLocalDate());
            }
            case MONTH -> {
                startDate = LocalDate.of(request.year(), request.month(), 1);
                endDate = startDate.plusMonths(1).minusDays(1);
                between = ChronoUnit.WEEKS.between(startDate, endDate) + 1;
                unit = ChronoUnit.WEEKS;
                dateToKey = date -> Integer.toString(date.get(weekFields.weekOfWeekBasedYear()));
                entryToKey = entry -> dateToKey.apply(entry.getAppliedAt());
            }
            case YEAR -> {
                startDate = LocalDate.of(request.year(), 1, 1);
                endDate = startDate.plusYears(1).minusDays(1);
                between = ChronoUnit.MONTHS.between(startDate, endDate) + 1;
                unit = ChronoUnit.MONTHS;
                dateToKey = date -> DateTimeFormatter.ofPattern("MMM").format(date);
                entryToKey = entry -> dateToKey.apply(entry.getAppliedAt().getMonth());
            }
            default -> throw new IllegalStateException();
        }

        var data = repository
                .findByAppliedDateIsBetween(startDate, endDate)
                .stream()
                .collect(Collectors.groupingBy(entryToKey, Collectors.summingInt(Entry::getAmount)));

        return Stream
                .iterate(startDate, d -> d.plus(1, unit))
                .limit(between)
                .map(date -> new TimeSeriesDataDTO(dateToKey.apply(date),
                        data.getOrDefault(dateToKey.apply(date), 0)))
                .collect(Collectors.toList());
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

package com.almgru.trabacco.controller;

import com.almgru.trabacco.data.EntryRepository;
import com.almgru.trabacco.dto.DurationDataDTO;
import com.almgru.trabacco.dto.TimeSeriesDataDTO;
import com.almgru.trabacco.entity.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
public class APIController {
    private final EntryRepository repository;

    @Autowired
    public APIController(EntryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("week-data")
    public List<TimeSeriesDataDTO> timeData(@RequestParam("year") Integer year, @RequestParam("week") Integer week) {
        long weeksInYear = IsoFields.WEEK_OF_WEEK_BASED_YEAR
                .rangeRefinedBy(LocalDate.of(year, 1, 1)).getMaximum();

        if (week < 1 || week > weeksInYear) {
            throw new IllegalArgumentException(String.format("No week %d in year %s.\n", week, year));
        }

        LocalDate firstDayOfWeek = LocalDate.of(year, 6, 1)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastDayOfWeek = firstDayOfWeek.plusDays(6);

        var weekData = repository
                .findByAppliedDateIsBetween(firstDayOfWeek, lastDayOfWeek)
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getAppliedAt().toLocalDate(),
                        Collectors.summingInt(Entry::getAmount)));

        return Stream
                .iterate(firstDayOfWeek, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(firstDayOfWeek, lastDayOfWeek) + 1)
                .map(date -> new TimeSeriesDataDTO(DateTimeFormatter.ISO_DATE.format(date),
                        weekData.getOrDefault(date, 0)))
                .collect(Collectors.toList());

    }

    @GetMapping("month-data")
    public List<TimeSeriesDataDTO> monthData(@RequestParam("year") Integer year, @RequestParam("month") Integer month,
                                             Locale locale) {
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);

        var weekFields = WeekFields.of(locale);
        var monthData = repository
                .findByAppliedDateIsBetween(firstDayOfMonth, lastDayOfMonth)
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getAppliedAt().get(weekFields.weekOfWeekBasedYear()),
                        Collectors.summingInt(Entry::getAmount)));

        return Stream
                .iterate(firstDayOfMonth, d -> d.plusWeeks(1))
                .limit(ChronoUnit.WEEKS.between(firstDayOfMonth, lastDayOfMonth) + 1)
                .map(date -> new TimeSeriesDataDTO(Integer.toString(date.get(weekFields.weekOfWeekBasedYear())),
                        monthData.getOrDefault(date.get(weekFields.weekOfWeekBasedYear()), 0)))
                .collect(Collectors.toList());
    }

    @GetMapping("year-data")
    public List<TimeSeriesDataDTO> yearData(@RequestParam("year") Integer year) {
        var firstDayOfYear = LocalDate.of(year, 1, 1);
        var lastDayOfYear = firstDayOfYear.plusYears(1).minusDays(1);

        var yearData = repository
                .findByAppliedDateIsBetween(firstDayOfYear, lastDayOfYear)
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getAppliedAt().getMonth(),
                        Collectors.summingInt(Entry::getAmount)));

        return Stream
                .iterate(firstDayOfYear, d -> d.plusMonths(1))
                .limit(ChronoUnit.MONTHS.between(firstDayOfYear, lastDayOfYear) + 1)
                .map(date -> new TimeSeriesDataDTO(DateTimeFormatter.ofPattern("MMM").format(date),
                        yearData.getOrDefault(date.getMonth(), 0)))
                .collect(Collectors.toList());
    }

    @GetMapping("fixed-data")
    public List<DurationDataDTO> fixedData() {
        return Arrays.asList(
            new DurationDataDTO("2021-03-29"),
            new DurationDataDTO("2021-03-30", 1.5, 1.3, 1.3, 2.1, 0.7, 2.0, 1.0, 1.4),
            new DurationDataDTO("2021-03-31", 0.7, 2.4, 1.6, 1.3, 1.2, 1.5, 1.1, 1.4, 2.0, 0.5, 1.1, 0.4),
            new DurationDataDTO("2021-04-01", 2.0, 1.8, 0.2, 1.2, 1.6, 1.8, 1.7, 1.4, 1.3, 0.3),
            new DurationDataDTO("2021-04-02", 2.0, 0.9, 1.7, 0.6, 1.4, 1.8, 1.5, 1.4, 0.8, 1.0, 0.3),
            new DurationDataDTO("2021-04-03", 2.1, 1.5, 0.9, 1.1, 1.2, 1.3, 1.2, 1.2, 1.9),
            new DurationDataDTO("2021-04-04", 1.4, 2.4, 1.7)
        );
    }
}

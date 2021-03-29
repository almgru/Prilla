package com.almgru.trabacco.controller;

import com.almgru.trabacco.data.EntryRepository;
import com.almgru.trabacco.dto.EntryDTO;
import com.almgru.trabacco.dto.WeekDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
public class API {
    private final EntryRepository repository;

    @Autowired
    public API(EntryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/entries")
    public List<EntryDTO> entries(@RequestParam(value = "page", required = false) Integer page) {
        page = page == null ? 1 : page;

        if (page < 1) {
            throw new IllegalArgumentException("Pages start at 1.");
        }

        Page<EntryDTO> result = repository.findAll(PageRequest.of(page - 1, 7))
                .map(entry -> new EntryDTO(entry.getInserted(), entry.getRemoved(), entry.getAmount()));

        if (page > result.getTotalPages()) {
            throw new IllegalArgumentException("No such page.");
        }

        return result.getContent();
    }

    @GetMapping("week-data")
    public List<WeekDataDTO> timeData(@RequestParam("year") Year year, @RequestParam("week") Integer week) {
        long weeksInYear = IsoFields.WEEK_OF_WEEK_BASED_YEAR
                .rangeRefinedBy(LocalDate.of(year.getValue(), 1, 1)).getMaximum();

        if (week < 1 || week > weeksInYear) {
            throw new IllegalArgumentException(String.format("No week %d in year %s.\n", week, year));
        }

        LocalDate firstDayOfWeek = LocalDate.now()
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastDayOfWeek = firstDayOfWeek.plusDays(6);

        List<WeekDataDTO> weekData = repository.findByDateBetweenGroupByDate(firstDayOfWeek, lastDayOfWeek);

        return Stream.iterate(firstDayOfWeek, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(firstDayOfWeek, lastDayOfWeek) + 1)
                .map(d -> new WeekDataDTO(d, weekData.stream()
                            .filter(dto -> dto.date().isEqual(d))
                            .mapToLong(WeekDataDTO::amount)
                            .sum()))
                .collect(Collectors.toList());
    }
}

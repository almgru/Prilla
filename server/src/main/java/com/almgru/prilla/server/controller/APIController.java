package com.almgru.prilla.server.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.almgru.prilla.server.data.EntryRepository;
import com.almgru.prilla.server.dto.DataRequestDTO;
import com.almgru.prilla.server.enums.TimeSpan;
import com.almgru.prilla.server.service.EntryGrouper;

@RestController
@RequestMapping("/api")
public class APIController {
    private final EntryRepository repository;
    private final EntryGrouper entryGrouper;

    @Autowired
    public APIController(final EntryRepository repository, final EntryGrouper entryGrouper) {
        this.repository = repository;
        this.entryGrouper = entryGrouper;
    }

    @GetMapping("amount-data")
    public Map<String, Integer> amountData(@Valid @ModelAttribute final DataRequestDTO request) {
        return entryGrouper.groupAmountByDate(
                repository.findByAppliedDateIsBetween(
                        request.start(),
                        getEndDateForRequest(request)),
                getMapperForTimeSpan(request.span()));
    }

    @GetMapping("duration-data")
    public Map<String, List<Long>> durationData(@Valid @ModelAttribute final DataRequestDTO request) {
        return entryGrouper.groupDurationSetByDate(
                repository.findByAppliedDateIsBetween(
                        request.start(),
                        getEndDateForRequest(request)),
                getMapperForTimeSpan(request.span()));
    }

    @GetMapping("duration-between-data")
    public Map<String, List<Long>> durationBetweenData(@Valid @ModelAttribute final DataRequestDTO request) {
        return entryGrouper.groupDurationBetweenSetByDate(
                repository.findByAppliedDateIsBetween(
                        request.start(),
                        getEndDateForRequest(request),
                        Sort.by("appliedDate").ascending().and(Sort.by("appliedTime").ascending())),
                getMapperForTimeSpan(request.span()));
    }

    private LocalDate getEndDateForRequest(final DataRequestDTO request) {
        return request.start().plus(1, getUnitForTimeSpan(request.span())).minusDays(1);
    }

    private TemporalUnit getUnitForTimeSpan(final TimeSpan span) {
        return switch (span) {
            case WEEK -> ChronoUnit.WEEKS;
            case MONTH -> ChronoUnit.MONTHS;
            case YEAR -> ChronoUnit.YEARS;
        };
    }

    private Function<LocalDate, String> getMapperForTimeSpan(final TimeSpan span) {
        return switch (span) {
            case WEEK -> DateTimeFormatter.ISO_DATE::format;
            case MONTH -> DateTimeFormatter.ofPattern("MM-'W'W").withLocale(Locale.forLanguageTag("en-SE"))::format;
            case YEAR -> DateTimeFormatter.ofPattern("yy-MM")::format;
        };
    }
}

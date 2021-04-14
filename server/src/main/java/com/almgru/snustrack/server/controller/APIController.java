package com.almgru.snustrack.server.controller;

import com.almgru.snustrack.server.data.EntryRepository;
import com.almgru.snustrack.server.dto.DataRequestDTO;
import com.almgru.snustrack.server.enums.TimeSpan;
import com.almgru.snustrack.server.service.EntryGrouper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@RestController
@RequestMapping("/api")
public class APIController {
    private final EntryRepository repository;
    private final EntryGrouper entryGrouper;

    @Autowired
    public APIController(EntryRepository repository, EntryGrouper entryGrouper) {
        this.repository = repository;
        this.entryGrouper = entryGrouper;
    }

    @GetMapping("amount-data")
    public Map<String, Integer> amountData(@Valid @ModelAttribute DataRequestDTO request) {
        return entryGrouper.groupAmountByDate(
                repository.findByAppliedDateIsBetween(
                        request.start(),
                        getEndDateForRequest(request)),
                getMapperForTimeSpan(request.span()));
    }

    @GetMapping("duration-data")
    public Map<String, Set<Long>> durationData(@Valid @ModelAttribute DataRequestDTO request) {
        return entryGrouper.groupDurationSetByDate(
                repository.findByAppliedDateIsBetween(
                        request.start(),
                        getEndDateForRequest(request)),
                getMapperForTimeSpan(request.span()));
    }

    private LocalDate getEndDateForRequest(DataRequestDTO request) {
        return request.start().plus(1, getUnitForTimeSpan(request.span())).minusDays(1);
    }

    private TemporalUnit getUnitForTimeSpan(TimeSpan span) {
        return switch (span) {
            case WEEK -> ChronoUnit.WEEKS;
            case MONTH -> ChronoUnit.MONTHS;
            case YEAR -> ChronoUnit.YEARS;
        };
    }

    private Function<LocalDate, String> getMapperForTimeSpan(TimeSpan span) {
        return switch (span) {
            case WEEK -> DateTimeFormatter.ISO_DATE::format;
            case MONTH -> DateTimeFormatter.ofPattern("MM-'W'W").withLocale(Locale.forLanguageTag("en-SE"))::format;
            case YEAR -> DateTimeFormatter.ofPattern("yy-MM")::format;
        };
    }
}

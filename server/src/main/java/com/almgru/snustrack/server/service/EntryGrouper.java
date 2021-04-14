package com.almgru.snustrack.server.service;

import com.almgru.snustrack.server.entity.Entry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EntryGrouper {
    public Map<String, Integer> groupAmountByDate(Collection<Entry> entries, Function<LocalDate, String> mapper) {
        return entries
                .stream()
                .collect(Collectors.groupingBy(entry -> mapper.apply(entry.getAppliedAt().toLocalDate()),
                        Collectors.summingInt(Entry::getAmount)));

    }

    public Map<String, Set<Long>> groupDurationSetByDate(Collection<Entry> entries, Function<LocalDate, String> mapper) {
        return entries
                .stream()
                .collect(Collectors.groupingBy(
                        entry -> mapper.apply(entry.getAppliedAt().toLocalDate()),
                        Collectors.mapping(
                                entry -> Duration.between(entry.getAppliedAt(), entry.getRemovedAt()).toMinutes(),
                                Collectors.toSet())));
    }
}

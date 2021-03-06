package com.almgru.prilla.server.service;

import com.almgru.prilla.server.entity.Entry;
import com.almgru.prilla.server.utility.OrderedPair;
import com.almgru.prilla.server.utility.Statistics;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class EntryGrouper {
    public Map<String, Integer> groupAmountByDate(Collection<Entry> entries, Function<LocalDate, String> mapper) {
        return entries
                .stream()
                .collect(Collectors.groupingBy(entry -> mapper.apply(entry.getAppliedAt().toLocalDate()),
                        Collectors.summingInt(Entry::getAmount)));

    }

    public Map<String, List<Long>> groupDurationSetByDate(Collection<Entry> entries, Function<LocalDate, String> mapper) {
        return entries
                .stream()
                .collect(Collectors.groupingBy(
                        entry -> mapper.apply(entry.getAppliedAt().toLocalDate()),
                        Collectors.mapping(
                                entry -> Duration.between(entry.getAppliedAt(), entry.getRemovedAt()).toMinutes(),
                                Collectors.toList())));
    }

    public Map<String, List<Long>> groupDurationBetweenSetByDate(List<Entry> entries, Function<LocalDate, String> mapper) {
        return removeOutliers(IntStream
                .range(1, entries.size())
                .mapToObj(i -> new OrderedPair<>(entries.get(i -1), entries.get(i)))
                .collect(Collectors.groupingBy(
                        pair -> mapper.apply(pair.left().getRemovedAt().toLocalDate()),
                        Collectors.mapping(
                                pair -> Duration.between(
                                        pair.left().getRemovedAt(), pair.right().getAppliedAt()).toMinutes(),
                                Collectors.toList()
                        )
                )));
    }

    private Map<String, List<Long>> removeOutliers(Map<String, List<Long>> data) {
        List<Long> allDataPoints = data.keySet().stream()
                .map(data::get)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (allDataPoints.isEmpty()) return data;

        double median = Statistics.median(allDataPoints);
        double mad = Statistics.medianAbsoluteDeviation(allDataPoints);

        data.keySet().forEach(key -> data.get(key).removeIf(dp -> Statistics.isSmallSampleOutlier(dp, median, mad)));

        return data;
    }
}

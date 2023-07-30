package com.almgru.prilla.server.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Map;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.almgru.prilla.server.entity.Entry;
import com.almgru.prilla.server.utility.OrderedPair;
import com.almgru.prilla.server.utility.Statistics;

@Service
public class EntryGrouper {
    public Map<String, Integer> groupAmountByDate(final Collection<Entry> entries, final Function<LocalDate, String> mapper) {
        return entries
                .stream()
                .collect(Collectors.groupingBy(entry -> mapper.apply(entry.getAppliedAt().toLocalDate()),
                        Collectors.summingInt(Entry::getAmount)));

    }

    public Map<String, List<Long>> groupDurationSetByDate(final Collection<Entry> entries, final Function<LocalDate, String> mapper) {
        return entries
                .stream()
                .collect(Collectors.groupingBy(
                        entry -> mapper.apply(entry.getAppliedAt().toLocalDate()),
                        Collectors.mapping(
                                entry -> Duration.between(entry.getAppliedAt(), entry.getRemovedAt()).toMinutes(),
                                Collectors.toList())));
    }

    public Map<String, List<Long>> groupDurationBetweenSetByDate(final List<Entry> entries, final Function<LocalDate, String> mapper) {
        return removeOutliers(IntStream
                .range(1, entries.size())
                .mapToObj(index -> new OrderedPair<>(entries.get(index - 1), entries.get(index)))
                .collect(Collectors.groupingBy(
                        pair -> mapper.apply(pair.left().getRemovedAt().toLocalDate()),
                        Collectors.mapping(
                                pair -> Duration.between(
                                        pair.left().getRemovedAt(), pair.right().getAppliedAt()).toMinutes(),
                                Collectors.toList()
                        )
                )));
    }

    private Map<String, List<Long>> removeOutliers(final Map<String, List<Long>> data) {
        final var allDataPoints = data.keySet().stream()
                .map(data::get)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (allDataPoints.isEmpty()) {
            return data;
        }

        final var median = Statistics.median(allDataPoints);
        final var mad = Statistics.medianAbsoluteDeviation(allDataPoints);

        data.keySet().forEach(key -> data.get(key).removeIf(dataPoint -> Statistics.isSmallSampleOutlier(dataPoint, median, mad)));

        return data;
    }
}

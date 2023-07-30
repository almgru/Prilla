package com.almgru.prilla.server.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Statistics {
    public static <T extends Number & Comparable<T>> double median(final List<T> population) {
        if (population.isEmpty()) {
            throw new IllegalArgumentException("Median is undefined for an empty collection.");
        }

        if (population.size() == 1) {
            return population.get(0).doubleValue();
        }

        final var copy = new ArrayList<>(population);
        Collections.sort(copy);

        if (population.size() % 2 == 1) {
            return copy.get(population.size() / 2).doubleValue();
        } else {
            return (copy.get(population.size() / 2 - 1).doubleValue() + copy.get(population.size() / 2).doubleValue()) / 2.0;
        }
    }

    public static double medianAbsoluteDeviation(final List<Long> population) {
        if (population.isEmpty()) {
            throw new IllegalArgumentException("Median absolute deviation is undefined for an empty collection.");
        }

        if (population.size() == 1) {
            return population.get(0);
        }

        final var median = Statistics.median(population);
        final var absoluteDeviations = population.stream()
                .map(dataPoint -> Math.abs(dataPoint - median))
                .collect(Collectors.toList());

        return Statistics.median(absoluteDeviations);
    }

    public static boolean isSmallSampleOutlier(final Long dataPoint, final double median, final double mad) {
        // https://stats.stackexchange.com/a/78617
        return ((0.6745 * (dataPoint - median)) / mad) > 3.5;
    }
}

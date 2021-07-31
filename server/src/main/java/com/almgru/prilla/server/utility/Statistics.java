package com.almgru.prilla.server.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Statistics {
    public static <T extends Number & Comparable<T>> double median(List<T> population) {
        if (population.isEmpty()) throw new IllegalArgumentException("Median is undefined for an empty collection.");
        if (population.size() == 1) return population.get(0).doubleValue();

        var copy = new ArrayList<>(population);
        Collections.sort(copy);

        if (population.size() % 2 == 1) {
            return copy.get(population.size() / 2).doubleValue();
        } else {
            return (copy.get(population.size() / 2 - 1).doubleValue() + copy.get(population.size() / 2).doubleValue()) / 2.0;
        }
    }

    public static double medianAbsoluteDeviation(List<Long> population) {
        if (population.isEmpty()) throw new IllegalArgumentException("MAD is undefined for an empty collection.");
        if (population.size() == 1) return population.get(0);

        var median = Statistics.median(population);
        var absoluteDeviations = population.stream()
                .map(dp -> Math.abs(dp - median))
                .collect(Collectors.toList());

        return Statistics.median(absoluteDeviations);
    }

    public static boolean isSmallSampleOutlier(Long dataPoint, double median, double mad) {
        // https://stats.stackexchange.com/a/78617
        return ((0.6745 * (dataPoint - median)) / mad) > 3.5;
    }
}

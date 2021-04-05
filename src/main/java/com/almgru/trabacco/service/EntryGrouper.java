package com.almgru.trabacco.service;

import com.almgru.trabacco.entity.Entry;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EntryGrouper {
    public <K extends Comparable<? super K>> Map<K, Integer> groupAmount(Collection<Entry> entries,
                                                                    Function<Entry, K> entryToKeyMapper,
                                                                    Iterator<K> it) {
        return groupAmount(entries, entryToKeyMapper, i -> i, it);
    }
    public <K extends Comparable<? super K>, I> Map<K, Integer> groupAmount(Collection<Entry> entries,
                                              Function<Entry, K> entryToKeyMapper,
                                              Function<I, K> iteratorValueToKeyMapper,
                                              Iterator<I> it) {
        var map = entries
                .stream()
                .collect(Collectors.groupingBy(entryToKeyMapper,
                        TreeMap::new,
                        Collectors.summingInt(Entry::getAmount)));

        while (it.hasNext()) {
            map.putIfAbsent(iteratorValueToKeyMapper.apply(it.next()), 0);
        }

        return map;
    }
}

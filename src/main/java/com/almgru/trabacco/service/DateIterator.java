package com.almgru.trabacco.service;

import java.time.LocalDate;
import java.time.temporal.TemporalUnit;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DateIterator implements Iterator<LocalDate> {

    private LocalDate current;
    private final LocalDate end;
    private final TemporalUnit step;

    public DateIterator(LocalDate start, LocalDate end, TemporalUnit step) {
        this.end = end;
        this.step = step;
        this.current = start;
    }

    @Override
    public boolean hasNext() {
        return !current.isAfter(end);
    }

    @Override
    public LocalDate next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return current = current.plus(1, this.step);
    }
}

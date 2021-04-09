package com.almgru.trabacco.server.service;

import com.almgru.trabacco.server.entity.Entry;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

@Service
public class TextFormatter {
    public String localizedTemporal(TemporalAccessor temporal, Locale locale, FormatStyle formatStyle) {
        return DateTimeFormatter
                .ofLocalizedDateTime(formatStyle)
                .withLocale(locale)
                .format(temporal);
    }

    public String entry(Entry entry, Locale locale) {
        var inserted = localizedTemporal(entry.getAppliedAt(), locale, FormatStyle.SHORT);
        var removed = localizedTemporal(entry.getRemovedAt(), locale, FormatStyle.SHORT);

        return String.format("%s — %s: %d %s", inserted, removed, entry.getAmount(),
                entry.getAmount() > 1 ? "portions" : "portion");
    }
}


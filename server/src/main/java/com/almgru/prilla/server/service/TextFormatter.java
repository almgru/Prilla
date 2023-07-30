package com.almgru.prilla.server.service;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.almgru.prilla.server.entity.Entry;

@Service
public class TextFormatter {
    public String localizedTemporal(final TemporalAccessor temporal, final Locale locale, final FormatStyle formatStyle) {
        return DateTimeFormatter
                .ofLocalizedDateTime(formatStyle)
                .withLocale(locale)
                .format(temporal);
    }

    public String entry(final Entry entry, final Locale locale) {
        final var inserted = localizedTemporal(entry.getAppliedAt(), locale, FormatStyle.SHORT);
        final var removed = localizedTemporal(entry.getRemovedAt(), locale, FormatStyle.SHORT);

        return String.format("%s — %s: %d %s", inserted, removed, entry.getAmount(),
                entry.getAmount() > 1 ? "portions" : "portion");
    }
}


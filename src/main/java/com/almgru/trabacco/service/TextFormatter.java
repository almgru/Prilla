package com.almgru.trabacco.service;

import com.almgru.trabacco.entity.Entry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@Service
public class TextFormatter {
    public String localizedDateTime(LocalDateTime dt, Locale locale, FormatStyle formatStyle) {
        return DateTimeFormatter
                .ofLocalizedDateTime(formatStyle)
                .withLocale(locale)
                .format(dt);
    }

    public String entry(Entry entry, Locale locale) {
        var inserted = localizedDateTime(entry.getInserted(), locale, FormatStyle.SHORT);
        var removed = localizedDateTime(entry.getRemoved(), locale, FormatStyle.SHORT);

        return String.format("%s — %s: %d %s", inserted, removed, entry.getAmount(),
                entry.getAmount() > 1 ? "portions" : "portion");
    }
}


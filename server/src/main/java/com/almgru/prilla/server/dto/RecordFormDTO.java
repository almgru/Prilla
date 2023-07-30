package com.almgru.prilla.server.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Positive;

import org.springframework.format.annotation.DateTimeFormat;

public record RecordFormDTO(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appliedDate,
        @DateTimeFormat(pattern = "HH:mm") LocalTime appliedTime,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate removedDate,
        @DateTimeFormat(pattern = "HH:mm") LocalTime removedTime,
        @Positive Integer amount
) {
    public static RecordFormDTO defaultValues() {
        return new RecordFormDTO(null, null, null, null, 1);
    }

    public static RecordFormDTO keepDates(final RecordFormDTO original) {
        final var defaultValues = RecordFormDTO.defaultValues();

        return new RecordFormDTO(
                original.appliedDate(), defaultValues.appliedTime(), original.removedDate(),
                defaultValues.removedTime(), defaultValues.amount()
        );
    }
}

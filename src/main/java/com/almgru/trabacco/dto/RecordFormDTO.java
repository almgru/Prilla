package com.almgru.trabacco.dto;

import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.time.LocalTime;

public record RecordFormDTO(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate insertedDate,
        @DateTimeFormat(pattern = "HH:mm") LocalTime insertedTime,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate removedDate,
        @DateTimeFormat(pattern = "HH:mm") LocalTime removedTime,
        @PositiveOrZero Integer amount
) {
    public static RecordFormDTO defaultValues() {
        return new RecordFormDTO(null, null, null, null, 1);
    }

    public static RecordFormDTO keepDates(RecordFormDTO original) {
        RecordFormDTO defaultValues = RecordFormDTO.defaultValues();

        return new RecordFormDTO(
                original.insertedDate(), defaultValues.insertedTime(), original.removedDate(),
                defaultValues.removedTime(), defaultValues.amount()
        );
    }
}

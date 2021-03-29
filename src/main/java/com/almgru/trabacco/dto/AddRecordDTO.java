package com.almgru.trabacco.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record AddRecordDTO(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate insertedDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime insertedTime,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate removedDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime removedTime,
        Integer amount
) { }

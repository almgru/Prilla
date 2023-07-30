package com.almgru.prilla.server.dto;

import com.almgru.prilla.server.enums.TimeSpan;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DataRequestDTO(
        @NotNull TimeSpan span,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start
) { }

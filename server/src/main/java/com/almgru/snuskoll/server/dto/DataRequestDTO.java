package com.almgru.snustrack.server.dto;

import com.almgru.snustrack.server.enums.TimeSpan;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public record DataRequestDTO(
        @NotNull TimeSpan span,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start
) { }

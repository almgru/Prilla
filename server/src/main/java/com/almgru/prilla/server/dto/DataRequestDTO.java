package com.almgru.prilla.server.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import com.almgru.prilla.server.enums.TimeSpan;

public record DataRequestDTO(
        @NotNull TimeSpan span,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start
) { }

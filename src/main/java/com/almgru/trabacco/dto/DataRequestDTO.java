package com.almgru.trabacco.dto;

import com.almgru.trabacco.enums.TimeSpan;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public record DataRequestDTO(
        @NotNull TimeSpan span,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start
) { }

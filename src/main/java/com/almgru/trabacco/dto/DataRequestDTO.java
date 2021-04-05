package com.almgru.trabacco.dto;

import com.almgru.trabacco.enums.TimeSpan;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public record DataRequestDTO(
        @RequestParam("span") @NotNull TimeSpan span,
        @RequestParam("year") @NotNull Integer year,
        @RequestParam(value = "month", required = false) @Min(1) @Max(12) Integer month,
        @RequestParam(value = "week", required = false) @Min(1) Integer week
) { }

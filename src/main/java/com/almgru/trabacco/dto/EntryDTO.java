package com.almgru.trabacco.dto;

import java.time.LocalDateTime;

public record EntryDTO(LocalDateTime appliedAt, LocalDateTime removedAt, Integer amount) {}

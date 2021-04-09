package com.almgru.trabacco.server.dto;

import java.time.LocalDateTime;

public record EntryDTO(LocalDateTime appliedAt, LocalDateTime removedAt, Integer amount) {}

package com.almgru.prilla.server.dto;

import java.time.LocalDateTime;

public record EntryDTO(LocalDateTime appliedAt, LocalDateTime removedAt, Integer amount) {}

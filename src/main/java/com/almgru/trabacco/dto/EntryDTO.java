package com.almgru.trabacco.dto;

import java.time.LocalDateTime;

public record EntryDTO(LocalDateTime inserted, LocalDateTime removed, int amount) { }
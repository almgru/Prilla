package com.almgru.prilla.server.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.almgru.prilla.server.dto.RecordFormDTO;
import com.almgru.prilla.server.dto.EntryDTO;
import com.almgru.prilla.server.dto.ListItemEntryDTO;
import com.almgru.prilla.server.entity.Entry;

@Service
public class EntryConverter {
    public EntryDTO entryToDTO(final Entry entry) {
        return new EntryDTO(entry.getAppliedAt(), entry.getRemovedAt(), entry.getAmount());
    }

    public ListItemEntryDTO entryToListItem(final Entry entry, final DateTimeFormatter formatter) {
        final var amount = entry.getAmount();

        return new ListItemEntryDTO(entry.getAppliedAt().format(formatter),
                entry.getRemovedAt().format(formatter),
                String.format("%d portion%s", amount, amount > 1 ? "s" : ""),
                entry.getId());
    }

    public Entry formDTOToEntry(final RecordFormDTO formDTO) {
        return new Entry(
                LocalDateTime.of(formDTO.appliedDate(), formDTO.appliedTime()),
                LocalDateTime.of(formDTO.removedDate(), formDTO.removedTime()),
                formDTO.amount()
        );
    }

    public Entry dtoToEntry(final EntryDTO dto) {
        return new Entry(dto.appliedAt(), dto.removedAt(), dto.amount());
    }
}

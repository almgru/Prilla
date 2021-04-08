package com.almgru.trabacco.service;

import com.almgru.trabacco.dto.EntryDTO;
import com.almgru.trabacco.dto.ListItemEntryDTO;
import com.almgru.trabacco.dto.RecordFormDTO;
import com.almgru.trabacco.entity.Entry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EntryConverter {
    public EntryDTO entryToDTO(Entry entry) {
        return new EntryDTO(entry.getAppliedAt(), entry.getRemovedAt(), entry.getAmount());
    }

    public ListItemEntryDTO entryToListItem(Entry entry, DateTimeFormatter formatter) {
        var amount = entry.getAmount();

        return new ListItemEntryDTO(entry.getAppliedAt().format(formatter),
                entry.getAppliedAt().format(formatter),
                String.format("%d portion%s", amount, amount > 1 ? "s" : ""));
    }

    public Entry formDTOToEntry(RecordFormDTO formDTO) {
        return new Entry(
                LocalDateTime.of(formDTO.appliedDate(), formDTO.appliedTime()),
                LocalDateTime.of(formDTO.removedDate(), formDTO.removedTime()),
                formDTO.amount()
        );
    }

    public Entry dtoToEntry(EntryDTO dto) {
        return new Entry(dto.appliedAt(), dto.removedAt(), dto.amount());
    }
}

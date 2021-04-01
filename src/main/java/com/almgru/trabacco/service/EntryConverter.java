package com.almgru.trabacco.service;

import com.almgru.trabacco.dto.EntryDTO;
import com.almgru.trabacco.dto.RecordFormDTO;
import com.almgru.trabacco.entity.Entry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EntryConverter {
    public EntryDTO entryToDTO(Entry entry) {
        return new EntryDTO(entry.getInserted(), entry.getRemoved(), entry.getAmount());
    }

    public Entry formDTOToEntry(RecordFormDTO formDTO) {
        return new Entry(
                LocalDateTime.of(formDTO.insertedDate(), formDTO.insertedTime()),
                LocalDateTime.of(formDTO.removedDate(), formDTO.removedTime()),
                formDTO.amount()
        );
    }
}

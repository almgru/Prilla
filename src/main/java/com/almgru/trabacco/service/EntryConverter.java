package com.almgru.trabacco.service;

import com.almgru.trabacco.dto.EntryDTO;
import com.almgru.trabacco.entity.Entry;
import org.springframework.stereotype.Service;

@Service
public class EntryConverter {
    public EntryDTO entryToDTO(Entry entry) {
        return new EntryDTO(entry.getInserted(), entry.getRemoved(), entry.getAmount());
    }
}

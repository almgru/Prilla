package com.almgru.trabacco.controller;

import com.almgru.trabacco.data.EntryRepository;
import com.almgru.trabacco.dto.EntryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class API {
    private final EntryRepository repository;

    @Autowired
    public API(EntryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/entries")
    public List<EntryDTO> entries(@RequestParam(value = "page", required = false) Integer page) {
        page = page == null ? 1 : page;

        if (page < 1) {
            throw new IllegalArgumentException("Pages start at 1.");
        }

        Page<EntryDTO> result = repository.findAll(PageRequest.of(page - 1, 7))
                .map(entry -> new EntryDTO(entry.getInserted(), entry.getRemoved(), entry.getAmount()));

        if (page > result.getTotalPages()) {
            throw new IllegalArgumentException("No such page.");
        }

        return result.getContent();
    }
}

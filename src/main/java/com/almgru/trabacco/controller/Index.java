package com.almgru.trabacco.controller;

import com.almgru.trabacco.data.EntryRepository;
import com.almgru.trabacco.dto.EntryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class Index {
    private final EntryRepository repository;

    @Autowired
    public Index(EntryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<EntryDTO> entries = repository.findAll(Sort.by("insertedDate").descending()
                    .and(Sort.by("insertedTime").descending()))
                .stream()
                .map(e -> new EntryDTO(e.getInserted(), e.getRemoved(), e.getAmount()))
                .collect(Collectors.toList());

        model.addAttribute("entries", entries);
        return "index";
    }
}

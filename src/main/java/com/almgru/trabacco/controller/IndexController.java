package com.almgru.trabacco.controller;

import com.almgru.trabacco.data.EntryRepository;
import com.almgru.trabacco.service.EntryConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.stream.Collectors;

@Controller
public class IndexController {
    private final EntryRepository repository;
    private final EntryConverter entryConverter;

    @Autowired
    public IndexController(EntryRepository repository, EntryConverter entryConverter) {
        this.repository = repository;
        this.entryConverter = entryConverter;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("entries", repository
                .findAll(Sort.by("appliedDate").descending().and(Sort.by("appliedTime").descending()))
                .stream()
                .map(entryConverter::entryToDTO)
                .collect(Collectors.toList())
        );

        return "index";
    }
}

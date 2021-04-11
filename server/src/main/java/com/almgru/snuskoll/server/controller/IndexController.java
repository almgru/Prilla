package com.almgru.snuskoll.server.controller;

import com.almgru.snuskoll.server.data.EntryRepository;
import com.almgru.snuskoll.server.service.EntryConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
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
    public String index(Model model, Locale locale) {
        model.addAttribute("entries", repository
                .findAll(Sort.by("appliedDate").descending().and(Sort.by("appliedTime").descending()))
                .stream()
                .map(e -> entryConverter.entryToListItem(e, DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                        .localizedBy(locale)))
                .collect(Collectors.toList())
        );

        return "index";
    }
}

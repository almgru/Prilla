package com.almgru.prilla.server.controller;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.almgru.prilla.server.data.EntryRepository;
import com.almgru.prilla.server.service.EntryConverter;
import com.almgru.prilla.server.service.TextFormatter;

@Controller
public class IndexController {
    private final EntryRepository repository;
    private final EntryConverter entryConverter;
    private final TextFormatter formatter;

    @Autowired
    public IndexController(final EntryRepository repository, final EntryConverter entryConverter, final TextFormatter formatter) {
        this.repository = repository;
        this.entryConverter = entryConverter;
        this.formatter = formatter;
    }

    @GetMapping("/")
    public String index(final Model model, final Locale locale) {
        model.addAttribute("entries", repository
                .findAll(Sort.by("appliedDate").descending().and(Sort.by("appliedTime").descending()))
                .stream()
                .map(entry -> entryConverter.entryToListItem(entry, DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                        .localizedBy(locale)))
                .collect(Collectors.toList())
        );

        return "index";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("id") final Integer id, final RedirectAttributes attr, final Locale locale) {
        final var entry = repository.findById(id);

        if (entry.isPresent()) {
            repository.deleteById(id);
            attr.addFlashAttribute("message",
                    String.format("Entry (%s) deleted!", formatter.entry(entry.get(), locale)));
        } else {
            attr.addFlashAttribute("message", String.format("No entry with id '%d' exists.", id));
        }

        return "redirect:/";
    }
}

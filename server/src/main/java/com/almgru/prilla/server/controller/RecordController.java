package com.almgru.prilla.server.controller;

import java.time.LocalDateTime;
import java.util.Locale;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.almgru.prilla.server.service.TextFormatter;
import com.almgru.prilla.server.data.EntryRepository;
import com.almgru.prilla.server.dto.RecordFormDTO;
import com.almgru.prilla.server.service.EntryConverter;

@Controller
public class RecordController {
    private final EntryRepository repository;
    private final EntryConverter entryConverter;
    private final TextFormatter formatter;

    @Autowired
    public RecordController(final EntryRepository repository, final EntryConverter entryConverter, final TextFormatter formatter) {
        this.repository = repository;
        this.entryConverter = entryConverter;
        this.formatter = formatter;
    }

    @GetMapping("/record")
    public String record(final Model model) {
        if (model.getAttribute("recordForm") == null) {
            model.addAttribute("recordForm", RecordFormDTO.defaultValues());
        }
        return "record";
    }

    @PostMapping("/record")
    public String record(
        @Valid @ModelAttribute("recordForm") final RecordFormDTO dto,
        final BindingResult bindingResult,
        final RedirectAttributes attr,
        final Locale locale
    ) {
        final var inserted = LocalDateTime.of(dto.appliedDate(), dto.appliedTime());
        final var removed = LocalDateTime.of(dto.removedDate(), dto.removedTime());

        if (removed.isBefore(inserted)) {
            bindingResult.addError(new FieldError("recordForm", "appliedDate",
                            "Must be before or equal to 'Removed at' date & time."));
            bindingResult.addError(new FieldError("recordForm", "appliedTime", ""));
            bindingResult.addError(new FieldError("recordForm", "removedDate",
                            "Must be after 'Applied at' date & time."));
            bindingResult.addError(new FieldError("recordForm", "removedTime", ""));
        }

        if (bindingResult.hasErrors()) {
            return "/record";
        }

        final var entry = entryConverter.formDTOToEntry(dto);
        repository.save(entry);

        attr.addFlashAttribute("recordForm", RecordFormDTO.keepDates(dto));
        attr.addFlashAttribute(
                "message",
                String.format("Entry (%s) recorded!", formatter.entry(entry, locale))
        );

        return "redirect:/record";
    }
}

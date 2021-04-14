package com.almgru.snustrack.server.controller;

import com.almgru.snustrack.server.data.EntryRepository;
import com.almgru.snustrack.server.dto.RecordFormDTO;
import com.almgru.snustrack.server.entity.Entry;
import com.almgru.snustrack.server.service.EntryConverter;
import com.almgru.snustrack.server.service.TextFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Locale;

@Controller
public class RecordController {
    private final EntryRepository repository;
    private final EntryConverter entryConverter;
    private final TextFormatter formatter;

    @Autowired
    public RecordController(EntryRepository repository, EntryConverter entryConverter, TextFormatter formatter) {
        this.repository = repository;
        this.entryConverter = entryConverter;
        this.formatter = formatter;
    }

    @GetMapping("/record")
    public String record(Model model) {
        if (model.getAttribute("recordForm") == null) {
            model.addAttribute("recordForm", RecordFormDTO.defaultValues());
        }
        return "record";
    }

    @PostMapping("/record")
    public String record(@Valid @ModelAttribute("recordForm") RecordFormDTO dto, BindingResult bindingResult,
                         RedirectAttributes attr, Locale locale) {
        LocalDateTime inserted = LocalDateTime.of(dto.appliedDate(), dto.appliedTime());
        LocalDateTime removed = LocalDateTime.of(dto.removedDate(), dto.removedTime());
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

        Entry entry = entryConverter.formDTOToEntry(dto);
        repository.save(entry);

        attr.addFlashAttribute("recordForm", RecordFormDTO.keepDates(dto));
        attr.addFlashAttribute(
                "message",
                String.format("Entry (%s) recorded!", formatter.entry(entry, locale))
        );

        //noinspection SpringMVCViewInspection
        return "redirect:/record";
    }
}

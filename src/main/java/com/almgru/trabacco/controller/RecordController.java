package com.almgru.trabacco.controller;

import com.almgru.trabacco.data.EntryRepository;
import com.almgru.trabacco.dto.RecordFormDTO;
import com.almgru.trabacco.entity.Entry;
import com.almgru.trabacco.service.EntryConverter;
import com.almgru.trabacco.service.TextFormatter;
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
        return "recordController";
    }

    @PostMapping("/record")
    public String record(@Valid @ModelAttribute("recordForm") RecordFormDTO dto, BindingResult bindingResult,
                         RedirectAttributes attr, Locale locale) {
        // TODO: Add custom validator
        LocalDateTime inserted = LocalDateTime.of(dto.appliedDate(), dto.appliedTime());
        LocalDateTime removed = LocalDateTime.of(dto.removedDate(), dto.removedTime());
        if (removed.isBefore(inserted) || removed.isEqual(inserted)) {
            bindingResult.addError(new FieldError("recordForm", "removedDate",
                            "Removed date & time must be after appliedAt date & time."));
            bindingResult.addError(new FieldError("recordForm", "removedTime",
                    "Removed date & time must be after appliedAt date & time."));
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

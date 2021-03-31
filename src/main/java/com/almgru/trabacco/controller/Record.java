package com.almgru.trabacco.controller;

import com.almgru.trabacco.data.EntryRepository;
import com.almgru.trabacco.dto.RecordFormDTO;
import com.almgru.trabacco.entity.Entry;
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

@Controller
public class Record {
    private final EntryRepository repository;

    @Autowired
    public Record(EntryRepository repository) {
        this.repository = repository;
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
                         RedirectAttributes attr) {
        // TODO: Add custom validator
        LocalDateTime inserted = LocalDateTime.of(dto.insertedDate(), dto.insertedTime());
        LocalDateTime removed = LocalDateTime.of(dto.removedDate(), dto.removedTime());
        if (removed.isBefore(inserted) || removed.isEqual(inserted)) {
            bindingResult.addError(new FieldError("recordForm", "removedDate",
                            "Removed date & time must be after inserted date & time."));
            bindingResult.addError(new FieldError("recordForm", "removedTime",
                    "Removed date & time must be after inserted date & time."));
        }

        if (bindingResult.hasErrors()) {
            return "/record";
        }

        // TODO: Add conversion service
        repository.save(new Entry(
                LocalDateTime.of(dto.insertedDate(), dto.insertedTime()),
                LocalDateTime.of(dto.removedDate(), dto.removedTime()),
                dto.amount()
        ));

        attr.addFlashAttribute("message", "Snus recorded successfully!");
        attr.addFlashAttribute("recordForm", RecordFormDTO.keepDates(dto));

        //noinspection SpringMVCViewInspection
        return "redirect:/record";
    }
}

package com.almgru.trabacco.controller;

import com.almgru.trabacco.data.EntryRepository;
import com.almgru.trabacco.dto.AddRecordDTO;
import com.almgru.trabacco.entity.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        return "record";
    }

    @PostMapping("/record")
    public String record(@ModelAttribute AddRecordDTO request, RedirectAttributes attr) {
        repository.save(new Entry(
                LocalDateTime.of(request.insertedDate(), request.insertedTime()),
                LocalDateTime.of(request.removedDate(), request.removedTime()),
                request.amount()
        ));

        attr.addFlashAttribute("message", "Snus recorded successfully!");

        // Keep value for insertedDate and removedDate
        attr.addFlashAttribute("insertedDate", request.insertedDate());
        attr.addFlashAttribute("removedDate", request.removedDate());

        //noinspection SpringMVCViewInspection
        return "redirect:/record";
    }
}

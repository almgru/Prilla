package com.almgru.trabacco.controller;

import com.almgru.trabacco.data.EntryRepository;
import com.almgru.trabacco.entity.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Map;

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
    public String record(@RequestParam("date") LocalDate date, @RequestParam("amount") int amount,
                             RedirectAttributes attr) {
        repository.save(new Entry(date, amount));
        attr.addFlashAttribute("message", "Snus recorded successfully!");
        return "redirect:/record";
    }
}

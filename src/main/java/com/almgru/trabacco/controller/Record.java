package com.almgru.trabacco.controller;

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
    private final Map<LocalDate, Integer> dateToSnusCount;

    @Autowired
    public Record(Map<LocalDate, Integer> dateToSnusCount) {
        this.dateToSnusCount = dateToSnusCount;
    }

    @GetMapping("/record")
    public String record(Model model) {
        return "record";
    }

    @PostMapping("/record")
    public String record(@RequestParam("date") LocalDate date, @RequestParam("amount") int amount,
                             RedirectAttributes attr) {
        Integer current = dateToSnusCount.get(date);
        current = current != null ? current : 0;
        dateToSnusCount.put(date, current + amount);
        attr.addFlashAttribute("message", "Snus recorded successfully!");
        return "redirect:/record";
    }
}

package com.almgru.trabacco.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.Map;

@Controller
public class Index {
    private final Map<LocalDate, Integer> dateToSnusCount;

    @Autowired
    public Index(Map<LocalDate, Integer> dateToSnusCount) {
        this.dateToSnusCount = dateToSnusCount;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("dateToSnusCount", dateToSnusCount);
        return "index";
    }
}

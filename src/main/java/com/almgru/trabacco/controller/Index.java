package com.almgru.trabacco.controller;

import com.almgru.trabacco.data.EntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Index {
    private final EntryRepository repository;

    @Autowired
    public Index(EntryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("entries", repository.findAll());
        return "index";
    }
}

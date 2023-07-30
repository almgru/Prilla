package com.almgru.prilla.server.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.almgru.prilla.server.data.EntryRepository;
import com.almgru.prilla.server.dto.EntryDTO;
import com.almgru.prilla.server.dto.RestoreBackupFormDTO;
import com.almgru.prilla.server.entity.Entry;
import com.almgru.prilla.server.service.EntryConverter;

@Controller
public class BackupRestoreController {
    private final EntryRepository repository;
    private final EntryConverter entryConverter;
    private final ObjectMapper mapper;

    public BackupRestoreController(final EntryRepository repository, final EntryConverter entryConverter, final ObjectMapper mapper) {
        this.repository = repository;
        this.entryConverter = entryConverter;
        this.mapper = mapper;
    }

    @GetMapping("/backup-restore")
    public String backupAndRestore(final Model model) {
        model.addAttribute("restoreForm", RestoreBackupFormDTO.empty());

        return "backup-restore";
    }

    @GetMapping("/download-backup")
    public ResponseEntity<byte[]> downloadBackup() throws JsonProcessingException {
        final var entries = repository
                .findAll(Sort.by("appliedDate").and(Sort.by("appliedTime")))
                .stream()
                .map(entryConverter::entryToDTO)
                .collect(Collectors.toList());

        final var buffer = mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(entries);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=backup.json")
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(buffer.length)
                .body(buffer);
    }

    @PostMapping("/restore-backup")
    public String restoreBackup(@ModelAttribute("restoreForm") final RestoreBackupFormDTO backupForm, final RedirectAttributes attr) throws IOException {
        final var fileContents = backupForm.backupFile().getBytes();
        final var entries = mapper.readValue(fileContents, new TypeReference<List<EntryDTO>>() {});
        final var existingEntries = repository.findAll().stream()
                .collect(Collectors.toMap(Entry::getAppliedAt, Entry::getRemovedAt));
        final var entriesToInsert = entries.stream()
                .filter(dto -> existingEntries.get(dto.appliedAt()) == null ||
                        !existingEntries.get(dto.appliedAt()).isEqual(dto.removedAt()))
                .map(entryConverter::dtoToEntry)
                .collect(Collectors.toList());

        if (entriesToInsert.isEmpty()) {
            attr.addFlashAttribute("message", "No unique entries to restore.");
        } else {
            repository.saveAll(entriesToInsert);
            attr.addFlashAttribute("message", String.format("%d entries restored!", entriesToInsert.size()));
        }

        return "redirect:/backup-restore";
    }
}

package com.almgru.trabacco.controller;

import com.almgru.trabacco.data.EntryRepository;
import com.almgru.trabacco.dto.EntryDTO;
import com.almgru.trabacco.service.EntryConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class BackupRestoreController {
    private final EntryRepository repository;
    private final EntryConverter entryConverter;
    private final ObjectMapper mapper;

    public BackupRestoreController(EntryRepository repository, EntryConverter entryConverter, ObjectMapper mapper) {
        this.repository = repository;
        this.entryConverter = entryConverter;
        this.mapper = mapper;
    }

    @GetMapping("/backup-restore")
    public String backupAndRestore() {
        return "backup-restore";
    }

    @GetMapping("/download-backup")
    public ResponseEntity<byte[]> downloadBackup() throws JsonProcessingException {
        List<EntryDTO> entries = repository
                .findAll(Sort.by("appliedDate").and(Sort.by("appliedTime")))
                .stream()
                .map(entryConverter::entryToDTO)
                .collect(Collectors.toList());

        byte[] buffer = mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(entries);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=backup.json")
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(buffer.length)
                .body(buffer);
    }
}

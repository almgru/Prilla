package com.almgru.trabacco.dto;

import org.springframework.web.multipart.MultipartFile;

public record RestoreBackupFormDTO(MultipartFile backupFile) {
    public static RestoreBackupFormDTO empty() {
        return new RestoreBackupFormDTO(null);
    }
}

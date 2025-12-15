package com.lotosia.profileservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir:uploads/profiles}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080/api/v1/files/profiles}")
    private String baseUrl;

    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Path uploadPath = Paths.get(uploadDir);

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = generateFileName(file.getOriginalFilename());
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = baseUrl + "/" + fileName;
            log.info("Profile picture saved: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("Error saving profile picture", e);
            throw new RuntimeException("Failed to save profile picture: " + e.getMessage(), e);
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}

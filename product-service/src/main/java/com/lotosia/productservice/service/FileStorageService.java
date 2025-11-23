package com.lotosia.productservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author: nijataghayev
 */

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir:uploads/products}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080/api/v1/files/products}")
    private String baseUrl;

    public List<String> saveFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }

        List<String> fileUrls = new ArrayList<>();
        Path uploadPath = Paths.get(uploadDir);

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String fileName = generateFileName(file.getOriginalFilename());
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    String fileUrl = baseUrl + "/" + fileName;
                    fileUrls.add(fileUrl);
                    log.info("File saved: {}", fileUrl);
                }
            }
        } catch (IOException e) {
            log.error("Error saving files", e);
            throw new RuntimeException("Failed to save files: " + e.getMessage(), e);
        }

        return fileUrls;
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}



package com.lotosia.productservice.service;

import com.lotosia.productservice.dto.media.MediaUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MediaService mediaService;
    private static final String PRODUCTS_DIRECTORY = "/products";

    public List<String> saveFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }

        List<String> filePaths = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                MediaUploadResponse response = mediaService.uploadProductImage(file, PRODUCTS_DIRECTORY);
                
                if (response != null && response.isSuccess() && response.getData() != null) {
                    String filePath = response.getData().getPath();
                    filePaths.add(filePath);
                } else {
                    throw new RuntimeException("Upload failed for file: " + file.getOriginalFilename());
                }
            }
        }

        return filePaths;
    }

    public void deleteFiles(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            return;
        }

        mediaService.deleteFiles(filePaths);
    }
}

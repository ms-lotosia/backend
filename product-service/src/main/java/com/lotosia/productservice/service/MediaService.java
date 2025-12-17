package com.lotosia.productservice.service;

import com.lotosia.productservice.client.MediaClient;
import com.lotosia.productservice.dto.media.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaClient mediaClient;

    public MediaUploadResponse uploadProductImage(MultipartFile file, String directory) {
        try {
            ResponseEntity<MediaUploadResponse> response = mediaClient.uploadImage(file, directory);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    public MediaFileListResponse getFiles(String directory) {
        try {
            ResponseEntity<MediaFileListResponse> response = mediaClient.getFileList(directory);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get file list: " + e.getMessage());
        }
    }

    public String getDownloadUrl(String fileId) {
        try {
            ResponseEntity<MediaDownloadResponse> response = mediaClient.getDownloadLink(fileId);
            MediaDownloadResponse body = response.getBody();
            if (body != null && body.getData() != null) {
                return body.getData().getDownloadUrl();
            }
            throw new RuntimeException("No download URL in response");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get download link: " + e.getMessage());
        }
    }

    public MediaDeleteResponse deleteFiles(List<String> filePaths) {
        try {
            MediaDeleteRequest request = MediaDeleteRequest.builder()
                    .filePaths(filePaths)
                    .build();
            ResponseEntity<MediaDeleteResponse> response = mediaClient.deleteFiles(request);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete files: " + e.getMessage());
        }
    }

    public MediaMoveResponse moveFile(String oldPath, String newPath, String newName) {
        try {
            MediaMoveRequest request = MediaMoveRequest.builder()
                    .oldPath(oldPath)
                    .newPath(newPath)
                    .newName(newName)
                    .build();
            ResponseEntity<MediaMoveResponse> response = mediaClient.moveFile(request);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to move file: " + e.getMessage());
        }
    }
}


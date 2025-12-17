package com.lotosia.productservice.client;

import com.lotosia.productservice.dto.media.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "media-service", url = "${media.service.url:http://media-service:8085}")
public interface MediaClient {

    @PostMapping(value = "/api/v1/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<MediaUploadResponse> uploadImage(
            @RequestPart("image") MultipartFile file,
            @RequestParam(required = false) String directory
    );

    @GetMapping("/api/v1/media/files")
    ResponseEntity<MediaFileListResponse> getFileList(
            @RequestParam(required = false, defaultValue = "/") String directory
    );

    @GetMapping("/api/v1/media/download/{fileId}")
    ResponseEntity<MediaDownloadResponse> getDownloadLink(
            @PathVariable("fileId") String fileId
    );

    @DeleteMapping("/api/v1/media/files")
    ResponseEntity<MediaDeleteResponse> deleteFiles(
            @RequestBody MediaDeleteRequest request
    );

    @PutMapping("/api/v1/media/move")
    ResponseEntity<MediaMoveResponse> moveFile(
            @RequestBody MediaMoveRequest request
    );
}


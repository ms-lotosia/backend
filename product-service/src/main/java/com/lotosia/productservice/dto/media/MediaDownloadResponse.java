package com.lotosia.productservice.dto.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaDownloadResponse {
    private boolean success;
    private DownloadData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DownloadData {
        private String downloadUrl;
        private String filename;
    }
}


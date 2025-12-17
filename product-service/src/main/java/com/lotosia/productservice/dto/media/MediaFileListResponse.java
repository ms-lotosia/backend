package com.lotosia.productservice.dto.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFileListResponse {
    private boolean success;
    private List<FileInfo> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        private String fs_id;
        private String path;
        private String server_filename;
        private Long size;
        private Long category;
        private String isdir;
    }
}


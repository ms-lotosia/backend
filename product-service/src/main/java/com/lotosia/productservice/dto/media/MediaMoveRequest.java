package com.lotosia.productservice.dto.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaMoveRequest {
    private String oldPath;
    private String newPath;
    private String newName;
}


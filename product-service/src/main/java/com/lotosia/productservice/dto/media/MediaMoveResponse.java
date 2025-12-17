package com.lotosia.productservice.dto.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaMoveResponse {
    private boolean success;
    private String message;
    private Object data;
}


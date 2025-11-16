package com.lotosia.supportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

/**
 * @author: nijataghayev
 */

@Data
@Builder
@FieldDefaults(level = PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class ResponseModel<T> {

    String message;
    T data;

    public static <T> ResponseModel<T> of(T data) {
        return ResponseModel.<T>builder()
                .message("OK")
                .data(data)
                .build();
    }
}

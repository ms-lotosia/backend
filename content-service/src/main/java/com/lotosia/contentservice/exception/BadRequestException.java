package com.lotosia.contentservice.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: nijataghayev
 */

@Getter
@Setter
public class BadRequestException extends RuntimeException {
    private String message;
    private String code;

    public BadRequestException(String code, String message) {
        super(code);
        this.code = code;
        this.message = message;
    }
}

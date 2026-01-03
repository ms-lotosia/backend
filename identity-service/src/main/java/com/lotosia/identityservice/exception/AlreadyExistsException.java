package com.lotosia.identityservice.exception;

public class AlreadyExistsException extends RuntimeException {

    private final String code;

    public AlreadyExistsException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

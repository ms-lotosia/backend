package com.lotosia.contentservice.exception;

/**
 * @author: nijataghayev
 */

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {

        super(message);
    }
}

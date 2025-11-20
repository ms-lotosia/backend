package com.lotosia.productservice.exception;

/**
 * @author: nijataghayev
 */


public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

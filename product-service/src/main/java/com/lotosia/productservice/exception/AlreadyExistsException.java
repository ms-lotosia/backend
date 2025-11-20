package com.lotosia.productservice.exception;

/**
 * @author: nijataghayev
 */

public class AlreadyExistsException extends RuntimeException {

    public AlreadyExistsException(String message) {
        super(message);
    }
}

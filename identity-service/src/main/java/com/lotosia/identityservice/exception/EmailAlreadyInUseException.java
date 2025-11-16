package com.lotosia.identityservice.exception;

/**
 * @author: nijataghayev
 */

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String message) {
        super(message);
    }
}

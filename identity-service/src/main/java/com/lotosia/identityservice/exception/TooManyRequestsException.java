package com.lotosia.identityservice.exception;

/**
 * @author: nijataghayev
 */

public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(String message) {
        super(message);
    }
}

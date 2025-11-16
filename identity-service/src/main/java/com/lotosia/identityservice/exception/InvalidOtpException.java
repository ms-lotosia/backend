package com.lotosia.identityservice.exception;

/**
 * @author: nijataghayev
 */

public class InvalidOtpException extends RuntimeException {

    public InvalidOtpException(String message) {
        super(message);
    }
}

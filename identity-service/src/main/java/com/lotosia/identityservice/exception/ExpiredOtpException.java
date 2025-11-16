package com.lotosia.identityservice.exception;

/**
 * @author: nijataghayev
 */

public class ExpiredOtpException extends RuntimeException {

    public ExpiredOtpException(String message) {
        super(message);
    }
}

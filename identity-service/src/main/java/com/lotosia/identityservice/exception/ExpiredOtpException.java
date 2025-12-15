package com.lotosia.identityservice.exception;
public class ExpiredOtpException extends RuntimeException {

    public ExpiredOtpException(String message) {
        super(message);
    }
}

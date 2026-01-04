package com.lotosia.identityservice.exception;

import com.lotosia.identityservice.dto.admin.AdminBootstrapResponse;

public class AdminAlreadyExistsException extends RuntimeException {
    private final AdminBootstrapResponse response;

    public AdminAlreadyExistsException(AdminBootstrapResponse response) {
        super(response.getMessage());
        this.response = response;
    }

    public AdminBootstrapResponse getResponse() {
        return response;
    }
}

package com.lotosia.identityservice.exception;

import com.lotosia.identityservice.dto.admin.AdminBootstrapResponse;

public class AdminUpgradeException extends RuntimeException {
    private final AdminBootstrapResponse response;

    public AdminUpgradeException(AdminBootstrapResponse response) {
        super(response.getMessage());
        this.response = response;
    }

    public AdminBootstrapResponse getResponse() {
        return response;
    }
}

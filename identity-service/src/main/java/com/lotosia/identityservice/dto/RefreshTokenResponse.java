package com.lotosia.identityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponse {

    private String accessToken;
    private String type = "Bearer";

    public RefreshTokenResponse(String accessToken) {
        this.accessToken = accessToken;
        this.type = "Bearer";
    }
}

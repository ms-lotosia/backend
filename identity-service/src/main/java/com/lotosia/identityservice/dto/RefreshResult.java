package com.lotosia.identityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshResult {
    private RefreshTokenResponse refreshTokenResponse;
    private String newRefreshToken;
}

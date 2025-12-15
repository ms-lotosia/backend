package com.lotosia.profileservice.dto.userpreference;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferenceRequest {

    @Size(max = 10)
    private String language;

    @Size(max = 5)
    private String currency;

    private Boolean emailNotification;
}

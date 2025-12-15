package com.lotosia.profileservice.dto.userpreference;

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
public class UserPreferenceResponse {

    private Long id;
    private Long profileId;
    private String language;
    private String currency;
    private Boolean emailNotification;
}

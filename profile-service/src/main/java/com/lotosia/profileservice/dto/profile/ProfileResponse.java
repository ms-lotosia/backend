package com.lotosia.profileservice.dto.profile;

import com.lotosia.profileservice.dto.userpreference.UserPreferenceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {

    private Long id;
    private Long userId;
    private String phoneNumber;
    private LocalDate birthDate;
    private String profileImageUrl;
    private UserPreferenceResponse userPreference;
}


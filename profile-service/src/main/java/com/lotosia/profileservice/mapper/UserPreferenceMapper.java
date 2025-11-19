package com.lotosia.profileservice.mapper;

import com.lotosia.profileservice.dto.UserPreferenceRequest;
import com.lotosia.profileservice.dto.UserPreferenceResponse;
import com.lotosia.profileservice.entity.Profile;
import com.lotosia.profileservice.entity.UserPreference;

public final class UserPreferenceMapper {

    private UserPreferenceMapper() {
    }

    public static UserPreference toEntity(Profile profile, UserPreferenceRequest request) {
        if (request == null) {
            return null;
        }
        UserPreference preference = new UserPreference();
        preference.setProfile(profile);
        preference.setLanguage(request.getLanguage());
        preference.setCurrency(request.getCurrency());
        preference.setEmailNotification(request.getEmailNotification());
        return preference;
    }

    public static void update(UserPreference preference, UserPreferenceRequest request) {
        if (preference == null || request == null) {
            return;
        }
        preference.setLanguage(request.getLanguage());
        preference.setCurrency(request.getCurrency());
        preference.setEmailNotification(request.getEmailNotification());
    }

    public static UserPreferenceResponse toResponse(UserPreference preference) {
        if (preference == null) {
            return null;
        }
        return UserPreferenceResponse.builder()
                .id(preference.getId())
                .profileId(preference.getProfile().getId())
                .language(preference.getLanguage())
                .currency(preference.getCurrency())
                .emailNotification(preference.getEmailNotification())
                .build();
    }
}


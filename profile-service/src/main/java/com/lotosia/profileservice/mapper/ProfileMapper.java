package com.lotosia.profileservice.mapper;

import com.lotosia.profileservice.dto.ProfileRequest;
import com.lotosia.profileservice.dto.ProfileResponse;
import com.lotosia.profileservice.entity.Profile;

public final class ProfileMapper {

    private ProfileMapper() {
    }

    public static Profile toEntity(ProfileRequest request) {
        Profile profile = new Profile();
        updateEntity(profile, request);
        return profile;
    }

    public static void updateEntity(Profile profile, ProfileRequest request) {
        if (request == null) {
            return;
        }
        profile.setUserId(request.getUserId());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setBirthDate(request.getBirthDate());
        profile.setProfileImageUrl(request.getProfileImageUrl());
    }

    public static ProfileResponse toResponse(Profile profile) {
        if (profile == null) {
            return null;
        }
        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .phoneNumber(profile.getPhoneNumber())
                .birthDate(profile.getBirthDate())
                .profileImageUrl(profile.getProfileImageUrl())
                .userPreference(UserPreferenceMapper.toResponse(profile.getUserPreference()))
                .build();
    }
}


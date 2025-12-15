package com.lotosia.profileservice.service;

import com.lotosia.profileservice.dto.profile.ProfileRequest;
import com.lotosia.profileservice.dto.profile.ProfileResponse;
import com.lotosia.profileservice.dto.userpreference.UserPreferenceResponse;
import com.lotosia.profileservice.entity.Profile;
import com.lotosia.profileservice.exception.ResourceNotFoundException;
import com.lotosia.profileservice.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final FileStorageService fileStorageService;

    public ProfileResponse create(ProfileRequest request) {
        Profile profile = new Profile();
        profile.setUserId(request.getUserId());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setBirthDate(request.getBirthDate());
        
        // Save profile picture and get URL
        String profileImageUrl = fileStorageService.saveFile(request.getProfileImageUrl());
        profile.setProfileImageUrl(profileImageUrl);
        
        Profile saved = profileRepository.save(profile);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getById(Long id) {
        return toResponse(getProfile(id));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for userId: " + userId));
        return toResponse(profile);
    }

    public ProfileResponse update(Long id, ProfileRequest request) {
        Profile profile = getProfile(id);
        profile.setUserId(request.getUserId());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setBirthDate(request.getBirthDate());
        
        // Save profile picture if provided
        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().isEmpty()) {
            String profileImageUrl = fileStorageService.saveFile(request.getProfileImageUrl());
            profile.setProfileImageUrl(profileImageUrl);
        }
        
        Profile saved = profileRepository.save(profile);
        return toResponse(saved);
    }

    public void delete(Long id) {
        Profile profile = getProfile(id);
        profileRepository.delete(profile);
    }

    private Profile getProfile(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + id));
    }

    private ProfileResponse toResponse(Profile profile) {
        if (profile == null) {
            return null;
        }
        
        UserPreferenceResponse userPreferenceResponse = null;
        if (profile.getUserPreference() != null) {
            userPreferenceResponse = UserPreferenceResponse.builder()
                    .id(profile.getUserPreference().getId())
                    .profileId(profile.getId())
                    .language(profile.getUserPreference().getLanguage())
                    .currency(profile.getUserPreference().getCurrency())
                    .emailNotification(profile.getUserPreference().getEmailNotification())
                    .build();
        }
        
        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .phoneNumber(profile.getPhoneNumber())
                .birthDate(profile.getBirthDate())
                .profileImageUrl(profile.getProfileImageUrl())
                .userPreference(userPreferenceResponse)
                .build();
    }
}

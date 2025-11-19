package com.lotosia.profileservice.service;

import com.lotosia.profileservice.dto.UserPreferenceRequest;
import com.lotosia.profileservice.dto.UserPreferenceResponse;
import com.lotosia.profileservice.entity.Profile;
import com.lotosia.profileservice.entity.UserPreference;
import com.lotosia.profileservice.exception.ResourceNotFoundException;
import com.lotosia.profileservice.repository.ProfileRepository;
import com.lotosia.profileservice.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: nijataghayev
 */

@Service
@RequiredArgsConstructor
@Transactional
public class UserPreferenceService {

    private final ProfileRepository profileRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    public UserPreferenceResponse upsert(Long profileId, UserPreferenceRequest request) {
        Profile profile = getProfile(profileId);
        UserPreference preference = userPreferenceRepository.findByProfileId(profileId)
                .orElseGet(() -> {
                    UserPreference newPreference = new UserPreference();
                    newPreference.setProfile(profile);
                    return newPreference;
                });

        preference.setLanguage(request.getLanguage());
        preference.setCurrency(request.getCurrency());
        preference.setEmailNotification(request.getEmailNotification());
        
        if (preference.getProfile() == null) {
            preference.setProfile(profile);
        }

        UserPreference saved = userPreferenceRepository.save(preference);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserPreferenceResponse getByProfileId(Long profileId) {
        UserPreference preference = userPreferenceRepository.findByProfileId(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Preferences not found for profileId: " + profileId));
        return toResponse(preference);
    }

    public void deleteByProfileId(Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile not found with id: " + profileId);
        }
        userPreferenceRepository.deleteByProfileId(profileId);
    }

    private Profile getProfile(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + profileId));
    }

    private UserPreferenceResponse toResponse(UserPreference preference) {
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

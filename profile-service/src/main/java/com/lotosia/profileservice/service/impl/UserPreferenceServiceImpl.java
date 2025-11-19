package com.lotosia.profileservice.service.impl;

import com.lotosia.profileservice.dto.UserPreferenceRequest;
import com.lotosia.profileservice.dto.UserPreferenceResponse;
import com.lotosia.profileservice.entity.Profile;
import com.lotosia.profileservice.entity.UserPreference;
import com.lotosia.profileservice.exception.ResourceNotFoundException;
import com.lotosia.profileservice.mapper.UserPreferenceMapper;
import com.lotosia.profileservice.repository.ProfileRepository;
import com.lotosia.profileservice.repository.UserPreferenceRepository;
import com.lotosia.profileservice.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final ProfileRepository profileRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    @Override
    public UserPreferenceResponse upsert(Long profileId, UserPreferenceRequest request) {
        Profile profile = getProfile(profileId);
        UserPreference preference = userPreferenceRepository.findByProfileId(profileId)
                .orElseGet(() -> UserPreferenceMapper.toEntity(profile, request));

        if (preference.getId() != null) {
            UserPreferenceMapper.update(preference, request);
        }

        if (preference.getProfile() == null) {
            preference.setProfile(profile);
        }

        UserPreference saved = userPreferenceRepository.save(preference);
        return UserPreferenceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPreferenceResponse getByProfileId(Long profileId) {
        UserPreference preference = userPreferenceRepository.findByProfileId(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Preferences not found for profileId: " + profileId));
        return UserPreferenceMapper.toResponse(preference);
    }

    @Override
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
}


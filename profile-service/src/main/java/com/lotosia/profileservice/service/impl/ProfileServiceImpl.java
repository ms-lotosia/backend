package com.lotosia.profileservice.service.impl;

import com.lotosia.profileservice.dto.ProfileRequest;
import com.lotosia.profileservice.dto.ProfileResponse;
import com.lotosia.profileservice.entity.Profile;
import com.lotosia.profileservice.exception.ResourceNotFoundException;
import com.lotosia.profileservice.mapper.ProfileMapper;
import com.lotosia.profileservice.repository.ProfileRepository;
import com.lotosia.profileservice.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;

    @Override
    public ProfileResponse create(ProfileRequest request) {
        Profile profile = ProfileMapper.toEntity(request);
        Profile saved = profileRepository.save(profile);
        return ProfileMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getById(Long id) {
        return ProfileMapper.toResponse(getProfile(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for userId: " + userId));
        return ProfileMapper.toResponse(profile);
    }

    @Override
    public ProfileResponse update(Long id, ProfileRequest request) {
        Profile profile = getProfile(id);
        ProfileMapper.updateEntity(profile, request);
        return ProfileMapper.toResponse(profileRepository.save(profile));
    }

    @Override
    public void delete(Long id) {
        Profile profile = getProfile(id);
        profileRepository.delete(profile);
    }

    private Profile getProfile(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + id));
    }
}


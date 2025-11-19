package com.lotosia.profileservice.service;

import com.lotosia.profileservice.dto.ProfileRequest;
import com.lotosia.profileservice.dto.ProfileResponse;

public interface ProfileService {

    ProfileResponse create(ProfileRequest request);

    ProfileResponse getById(Long id);

    ProfileResponse getByUserId(Long userId);

    ProfileResponse update(Long id, ProfileRequest request);

    void delete(Long id);
}


package com.lotosia.profileservice.service;

import com.lotosia.profileservice.dto.UserPreferenceRequest;
import com.lotosia.profileservice.dto.UserPreferenceResponse;

public interface UserPreferenceService {

    UserPreferenceResponse upsert(Long profileId, UserPreferenceRequest request);

    UserPreferenceResponse getByProfileId(Long profileId);

    void deleteByProfileId(Long profileId);
}


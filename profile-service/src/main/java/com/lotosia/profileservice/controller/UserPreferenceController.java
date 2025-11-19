package com.lotosia.profileservice.controller;

import com.lotosia.profileservice.dto.UserPreferenceRequest;
import com.lotosia.profileservice.dto.UserPreferenceResponse;
import com.lotosia.profileservice.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @PostMapping("/{profileId}")
    public ResponseEntity<UserPreferenceResponse> createOrUpdate(@PathVariable Long profileId,
                                                                 @Valid @RequestBody UserPreferenceRequest request) {
        return ResponseEntity.ok(userPreferenceService.upsert(profileId, request));
    }

    @PutMapping("/{profileId}")
    public ResponseEntity<UserPreferenceResponse> update(@PathVariable Long profileId,
                                                         @Valid @RequestBody UserPreferenceRequest request) {
        return ResponseEntity.ok(userPreferenceService.upsert(profileId, request));
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<UserPreferenceResponse> get(@PathVariable Long profileId) {
        return ResponseEntity.ok(userPreferenceService.getByProfileId(profileId));
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> delete(@PathVariable Long profileId) {
        userPreferenceService.deleteByProfileId(profileId);
        return ResponseEntity.noContent().build();
    }
}


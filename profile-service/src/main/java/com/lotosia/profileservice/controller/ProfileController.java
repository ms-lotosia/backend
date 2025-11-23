package com.lotosia.profileservice.controller;

import com.lotosia.profileservice.dto.profile.ProfileRequest;
import com.lotosia.profileservice.dto.profile.ProfileResponse;
import com.lotosia.profileservice.service.ProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile Controller")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProfileResponse> create(@Valid @ModelAttribute ProfileRequest request) {
        return ResponseEntity.ok(profileService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getByUserId(userId));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ProfileResponse> update(@PathVariable Long id,
                                                  @Valid @ModelAttribute ProfileRequest request) {
        return ResponseEntity.ok(profileService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        profileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}


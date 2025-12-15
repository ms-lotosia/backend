package com.lotosia.identityservice.client;

import com.lotosia.identityservice.dto.ProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
@FeignClient(name = "profile-service", path = "/api/v1/profiles")
public interface ProfileClient {

    @PostMapping
    void createProfile(@RequestBody ProfileRequest profileRequest);
}

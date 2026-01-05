package com.lotosia.identityservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "profile-service", path = "/api/v1/profiles")
@CircuitBreaker(name = "profileService")
@TimeLimiter(name = "profileService")
public interface ProfileClient {

    @PostMapping("/internal")
    void createProfile(@RequestBody CreateProfileRequest profileRequest);
}


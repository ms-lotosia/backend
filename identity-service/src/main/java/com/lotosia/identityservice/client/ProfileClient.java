package com.lotosia.identityservice.client;

import com.lotosia.identityservice.dto.ProfileRequest;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author: nijataghayev
 */

@FeignClient(name = "profile-service", path = "/api/v1/profiles")
public interface ProfileClient {

    @PostMapping()
    void createProfile(@RequestBody ProfileRequest profileRequest);
}

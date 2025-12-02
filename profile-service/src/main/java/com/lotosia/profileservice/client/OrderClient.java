package com.lotosia.profileservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author: nijataghayev
 */

@FeignClient(name = "shopping-service", path = "/api/v1/orders")
public interface OrderClient {


    @GetMapping("/hasReceived")
    boolean hasUserReceivedProduct(
            @RequestParam("userId") Long userId,
            @RequestParam("productId") Long productId
    );
}

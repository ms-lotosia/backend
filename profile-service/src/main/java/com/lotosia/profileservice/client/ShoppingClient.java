package com.lotosia.profileservice.client;

import com.lotosia.profileservice.dto.review.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author: nijataghayev
 */

@FeignClient(name = "shopping-service", path = "/api/v1/orders")
public interface ShoppingClient {

    @GetMapping("/internal/orders/{orderId}")
    OrderResponse getOrder(@PathVariable Long orderId);
}

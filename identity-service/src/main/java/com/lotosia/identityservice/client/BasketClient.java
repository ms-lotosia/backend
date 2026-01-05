package com.lotosia.identityservice.client;

import com.lotosia.identityservice.dto.CreateBasketRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
@FeignClient(name = "shopping-service", path = "/api/v1/baskets")
@CircuitBreaker(name = "basketService")
@TimeLimiter(name = "basketService")
public interface BasketClient {

    @PostMapping()
    void createBasket(@RequestBody CreateBasketRequest dto);
}

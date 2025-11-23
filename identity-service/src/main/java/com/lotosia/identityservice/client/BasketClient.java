package com.lotosia.identityservice.client;

import com.lotosia.identityservice.dto.CreateBasketRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author: nijataghayev
 */

@FeignClient(name = "shopping-service", path = "/api/v1/baskets")
public interface BasketClient {

    @PostMapping()
    void createBasket(@RequestBody CreateBasketRequest dto);
}

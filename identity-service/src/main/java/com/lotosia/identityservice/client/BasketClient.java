package com.lotosia.identityservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author: nijataghayev
 */

@FeignClient(name = "shopping-service", path = "/api/v1/baskets")
public interface BasketClient {

//    @PostMapping()

}

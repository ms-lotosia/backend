package com.lotosia.identityservice.client;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author: nijataghayev
 */

@FeignClient(name = "shopping-service", url = "/api/v1/orders")
public interface OrderClient {


}

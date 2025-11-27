package com.lotosia.shoppingservice.client;

import com.lotosia.shoppingservice.dto.order.AddressResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author: nijataghayev
 */

@FeignClient(name = "profile-service", url = "/api/v1/addresses")
public interface AddressClient {

    @GetMapping("{id}")
    AddressResponse getAddress(@PathVariable Long id);
}

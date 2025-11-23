package com.lotosia.shoppingservice.client;

import com.lotosia.shoppingservice.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author: nijataghayev
 */

@FeignClient(name = "product-service", path = "/v1/api/products")
public interface ProductClient {

    @GetMapping("/{id}")
    ProductResponse getProductById(@PathVariable Long id);
}

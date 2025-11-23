package com.lotosia.profileservice.client;


import com.lotosia.profileservice.dto.favoriteproduct.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", path = "/api/v1/products")
public interface ProductClient {
    @GetMapping("/{id}")
    ProductResponse getProductById(@PathVariable Long id);
}

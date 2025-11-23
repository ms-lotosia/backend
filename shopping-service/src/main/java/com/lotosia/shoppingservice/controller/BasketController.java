package com.lotosia.shoppingservice.controller;

import com.lotosia.shoppingservice.dto.basket.CreateBasketRequest;
import com.lotosia.shoppingservice.service.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: nijataghayev
 */

@RestController
@RequestMapping("/api/v1/baskets")
@RequiredArgsConstructor
@Tag(name = "Basket Controller")
public class BasketController {

    private final BasketService basketService;

    @Operation(summary = "Create a new basket")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createBasket(@Valid @RequestBody CreateBasketRequest request) {
        basketService.createBasket(request.getUserId());
    }
}


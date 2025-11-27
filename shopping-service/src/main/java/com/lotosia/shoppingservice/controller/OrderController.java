package com.lotosia.shoppingservice.controller;

import com.lotosia.shoppingservice.dto.order.CreateOrderRequest;
import com.lotosia.shoppingservice.dto.order.OrderResponse;
import com.lotosia.shoppingservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: nijataghayev
 */

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private OrderService orderService;

    @PostMapping("/create")
    public OrderResponse create(@RequestHeader("X-User-Id") Long userId,
                                @RequestBody CreateOrderRequest request) {

        return orderService.createOrder(userId, request);
    }
}

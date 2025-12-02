package com.lotosia.shoppingservice.controller;

import com.lotosia.shoppingservice.dto.order.CreateOrderRequest;
import com.lotosia.shoppingservice.dto.order.OrderResponse;
import com.lotosia.shoppingservice.dto.order.OrderStatusUpdate;
import com.lotosia.shoppingservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long orderId,
                                          @RequestBody OrderStatusUpdate request) {

        orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok("Status updated");
    }

    @GetMapping("/hasReceived")
    public ResponseEntity<Boolean> hasUserReceivedProduct(
            @RequestParam Long userId,
            @RequestParam Long productId
    ) {
        boolean delivered = orderService.hasUserReceivedProduct(userId, productId);
        return ResponseEntity.ok(delivered);
    }
}

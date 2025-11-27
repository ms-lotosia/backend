package com.lotosia.shoppingservice.service;

import com.lotosia.shoppingservice.client.AddressClient;
import com.lotosia.shoppingservice.client.ProductClient;
import com.lotosia.shoppingservice.dto.basket.ProductResponse;
import com.lotosia.shoppingservice.dto.order.AddressResponse;
import com.lotosia.shoppingservice.dto.order.CreateOrderRequest;
import com.lotosia.shoppingservice.dto.order.OrderItemResponse;
import com.lotosia.shoppingservice.dto.order.OrderResponse;
import com.lotosia.shoppingservice.entity.Basket;
import com.lotosia.shoppingservice.entity.Order;
import com.lotosia.shoppingservice.entity.OrderItem;
import com.lotosia.shoppingservice.enums.OrderStatus;
import com.lotosia.shoppingservice.exception.ResourceNotFoundException;
import com.lotosia.shoppingservice.repository.BasketRepository;
import com.lotosia.shoppingservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: nijataghayev
 */

@Service
@RequiredArgsConstructor
public class OrderService {

    private final BasketRepository basketRepository;
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final AddressClient addressClient;

    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        Basket basket = basketRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Basket not found"));

        if (basket.getProductQuantities().isEmpty()) {
            throw new IllegalStateException("Basket is empty");
        }

        AddressResponse address = addressClient.getAddress(request.getAddressId());

        String deliveryAddress = address.getCity() + ", " + address.getAddressLine1();

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PREPARING);
        order.setDeliveryAddress(deliveryAddress);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var entry : basket.getProductQuantities().entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            ProductResponse product = productClient.getProductById(productId);

            BigDecimal totalPrice = product.getPrice()
                    .multiply(BigDecimal.valueOf(quantity));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(productId);
            item.setPrice(product.getPrice());
            item.setProductName(product.getName());
            item.setQuantity(quantity);
            item.setTotalPrice(totalPrice);

            items.add(item);

            totalAmount = totalAmount.add(totalPrice);
        }

        order.setItems(items);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        basket.getProductQuantities().clear();
        basketRepository.save(basket);

        return mapToDto(savedOrder);
    }

    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        switch (newStatus) {

            case PREPARING:
                throw new RuntimeException("Bu status manual olaraq verilə bilməz");

            case IN_COURIER:
                if (order.getStatus() != OrderStatus.PREPARING) {
                    throw new RuntimeException("Sifariş yalnız PREPARING olduqda kuryerə verilə bilər");
                }
                break;

            case DELIVERED:
                if (order.getStatus() != OrderStatus.IN_COURIER) {
                    throw new RuntimeException("Sifariş yalnız kuryerdə olduqda çatdırılmış kimi qeyd edilə bilər");
                }
                break;

            default:
                throw new RuntimeException("Uyğunsuz status");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    private OrderResponse mapToDto(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .deliveryAddress(order.getDeliveryAddress())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(
                        order.getItems().stream().map(i ->
                                OrderItemResponse.builder()
                                        .productId(i.getProductId())
                                        .productName(i.getProductName())
                                        .price(i.getPrice())
                                        .quantity(i.getQuantity())
                                        .totalPrice(i.getTotalPrice())
                                        .build()
                        ).toList()
                )
                .build();
    }
}

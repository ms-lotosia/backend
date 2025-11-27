package com.lotosia.shoppingservice.service;

import com.lotosia.shoppingservice.client.ProductClient;
import com.lotosia.shoppingservice.dto.basket.BasketProductDto;
import com.lotosia.shoppingservice.dto.basket.BasketResponseDto;
import com.lotosia.shoppingservice.dto.basket.CreateBasketRequest;
import com.lotosia.shoppingservice.dto.basket.ProductResponse;
import com.lotosia.shoppingservice.entity.Basket;
import com.lotosia.shoppingservice.exception.ResourceNotFoundException;
import com.lotosia.shoppingservice.repository.BasketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: nijataghayev
 */

@Service
@RequiredArgsConstructor
public class BasketService {

    private final BasketRepository basketRepository;
    private final ProductClient productClient;

    public void createBasket(CreateBasketRequest createBasketRequest) {
        Basket basket = new Basket();
        basket.setUserId(createBasketRequest.getUserId());
        basket.setProductQuantities(new HashMap<>());

        basketRepository.save(basket);
    }

    public BasketResponseDto addProductToBasket(Long productId, Long userId, Integer quantity) {
        Basket basket = basketRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Basket not found"));

        int currentQuantity = basket.getProductQuantities()
                .getOrDefault(productId, 0);

        basket.getProductQuantities()
                .put(productId, currentQuantity + quantity);

        basketRepository.save(basket);

        return getBasket(userId);
    }

    public BasketResponseDto removeProductFromBasket(Long productId, Long userId) {
        Basket basket = basketRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Basket not found"));

        Map<Long, Integer> products = basket.getProductQuantities();

        if(!products.containsKey(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }

        products.remove(productId);

        basketRepository.save(basket);

        return getBasket(userId);
    }

    public BasketResponseDto getBasket(Long userId) {
        Basket basket = basketRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Basket not found"));

        List<BasketProductDto> productDtos = basket.getProductQuantities()
                .entrySet()
                .stream()
                .map(entry -> {
                    ProductResponse product = productClient.getProductById(entry.getKey());
                    return BasketProductDto.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .price(product.getPrice())
                            .quantity(entry.getValue())
                            .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(entry.getValue())))
                            .build();
                })
                .toList();

        BigDecimal totalPrice = productDtos.stream()
                .map(BasketProductDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = productDtos.stream()
                .mapToInt(BasketProductDto::getQuantity)
                .sum();

        return BasketResponseDto.builder()
                .id(basket.getId())
                .userId(basket.getUserId())
                .products(productDtos)
                .totalItems(totalItems)
                .totalPrice(totalPrice)
                .build();
    }

    public void clearBasket(Long userId){
        basketRepository.findByUserId(userId).ifPresent(b -> {
            b.setProductQuantities(new HashMap<>());
            basketRepository.save(b);
        });
    }
}

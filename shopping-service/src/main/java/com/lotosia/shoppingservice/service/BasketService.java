package com.lotosia.shoppingservice.service;

import com.lotosia.shoppingservice.dto.basket.BasketResponseDto;
import com.lotosia.shoppingservice.entity.Basket;
import com.lotosia.shoppingservice.repository.BasketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author: nijataghayev
 */

@Service
@RequiredArgsConstructor
public class BasketService {

    private final BasketRepository basketRepository;

    public void createBasket(Long userId) {
        Basket basket = new Basket();
        basket.setUserId(userId);
        basket.setProductQuantities(new HashMap<>());

        basketRepository.save(basket);
    }

//    public BasketResponseDto getBasket(Long userId) {
//        Basket basket = basketRepository.findByUserId(userId);
//    }
}

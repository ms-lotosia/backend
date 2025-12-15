package com.lotosia.shoppingservice.dto.basket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketItemResponse {

    private ProductResponse product;

    private Integer quantity;
}

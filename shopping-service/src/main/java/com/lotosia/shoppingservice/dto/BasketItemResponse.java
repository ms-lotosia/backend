package com.lotosia.shoppingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: nijataghayev
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketItemResponse {

    private ProductResponse product;

    private Integer quantity;
}

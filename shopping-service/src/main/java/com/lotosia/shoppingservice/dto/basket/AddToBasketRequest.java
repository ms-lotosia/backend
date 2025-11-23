package com.lotosia.shoppingservice.dto.basket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: nijataghayev
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddToBasketRequest {

    private Long productId;
    private Integer quantity;
}

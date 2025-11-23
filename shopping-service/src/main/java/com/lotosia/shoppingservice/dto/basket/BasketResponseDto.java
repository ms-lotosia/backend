package com.lotosia.shoppingservice.dto.basket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: nijataghayev
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasketResponseDto {

    private Long id;
    private Long userId;
    private List<BasketProductDto> products;
    private Integer totalItems;
    private Double totalPrice;
}

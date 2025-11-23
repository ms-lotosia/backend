package com.lotosia.productservice.dto.product;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: nijataghayev
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProduct {

    @Positive
    private Long id;

    private String name;

    private String description;

    @Positive
    private BigDecimal price;

    private List<String> images;

    private Long categoryId;
}



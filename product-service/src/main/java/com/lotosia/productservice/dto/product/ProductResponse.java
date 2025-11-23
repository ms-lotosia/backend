package com.lotosia.productservice.dto.product;

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
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private List<String> images;
    private Long categoryId;
    private String categoryName;
}


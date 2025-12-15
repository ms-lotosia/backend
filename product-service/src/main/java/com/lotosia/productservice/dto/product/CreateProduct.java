package com.lotosia.productservice.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProduct {

    @NotNull
    @Size(min = 1, max = 255)
    private String name;

    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    private MultipartFile[] images;

    @NotNull
    private Long categoryId;
}

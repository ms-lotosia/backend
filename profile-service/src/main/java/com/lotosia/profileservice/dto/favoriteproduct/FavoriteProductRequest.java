package com.lotosia.profileservice.dto.favoriteproduct;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class FavoriteProductRequest {
    @NotNull
    @Positive
    private Long productId;
}

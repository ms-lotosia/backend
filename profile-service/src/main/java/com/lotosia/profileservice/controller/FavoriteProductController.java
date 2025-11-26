package com.lotosia.profileservice.controller;

import com.lotosia.profileservice.dto.favoriteproduct.FavoriteProductRequest;
import com.lotosia.profileservice.dto.favoriteproduct.FavoriteProductResponse;
import com.lotosia.profileservice.service.FavoriteProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: nijataghayev
 */

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorite Product Controller")
public class FavoriteProductController {

    private final FavoriteProductService favoriteProductService;

    @PostMapping("/users/{userId}")
    public ResponseEntity<FavoriteProductResponse> addFavorite(
            @PathVariable Long userId,
            @RequestBody @Valid FavoriteProductRequest request) {
        return ResponseEntity.ok(favoriteProductService.addFavorite(userId, request));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<FavoriteProductResponse>> getFavorites(@PathVariable Long userId) {
        List<FavoriteProductResponse> favorites = favoriteProductService.getFavoritesByUserId(userId);

        if (favorites.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(favorites);
    }

    @DeleteMapping("/users/{userId}/products/{productId}")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        favoriteProductService.removeFavorite(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}/products/{productId}/check")
    public ResponseEntity<Boolean> isFavorite(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(favoriteProductService.isFavorite(userId, productId));
    }
}


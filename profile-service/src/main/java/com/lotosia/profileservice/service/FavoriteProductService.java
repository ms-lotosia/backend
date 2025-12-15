package com.lotosia.profileservice.service;

import com.lotosia.profileservice.client.ProductClient;
import com.lotosia.profileservice.dto.favoriteproduct.FavoriteProductRequest;
import com.lotosia.profileservice.dto.favoriteproduct.FavoriteProductResponse;
import com.lotosia.profileservice.dto.favoriteproduct.ProductResponse;
import com.lotosia.profileservice.entity.FavoriteProduct;
import com.lotosia.profileservice.exception.ResourceNotFoundException;
import com.lotosia.profileservice.repository.FavoriteProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteProductService {

    private final FavoriteProductRepository favoriteProductRepository;
    private final ProductClient productClient;

    public FavoriteProductResponse addFavorite(Long userId, FavoriteProductRequest request) {
        if (favoriteProductRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new IllegalArgumentException("Product is already in favorites");
        }

        ProductResponse product = productClient.getProductById(request.getProductId());
        if (product == null) {
            throw new ResourceNotFoundException("Product not found with id: " + request.getProductId());
        }

        FavoriteProduct favoriteProduct = new FavoriteProduct();
        favoriteProduct.setUserId(userId);
        favoriteProduct.setProductId(request.getProductId());

        FavoriteProduct saved = favoriteProductRepository.save(favoriteProduct);
        return mapToResponse(saved, product);
    }

    @Transactional(readOnly = true)
    public List<FavoriteProductResponse> getFavoritesByUserId(Long userId) {
        List<FavoriteProduct> favorites = favoriteProductRepository.findByUserId(userId);
        return favorites.stream()
                .map(fp -> {
                    ProductResponse product = productClient.getProductById(fp.getProductId());
                    return mapToResponse(fp, product);
                })
                .toList();
    }

    public void removeFavorite(Long userId, Long productId) {
        FavoriteProduct favoriteProduct = favoriteProductRepository
                .findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Favorite product not found for user: " + userId + " and product: " + productId));

        favoriteProductRepository.delete(favoriteProduct);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long productId) {
        return favoriteProductRepository.existsByUserIdAndProductId(userId, productId);
    }

    private FavoriteProductResponse mapToResponse(FavoriteProduct favoriteProduct, ProductResponse product) {
        return FavoriteProductResponse.builder()
                .id(favoriteProduct.getId())
                .productId(favoriteProduct.getProductId())
                .productName(product != null ? product.getName() : null)
                .productDescription(product != null ? product.getDescription() : null)
                .productPrice(product != null ? product.getPrice() : null)
                .productImages(product != null ? product.getImages() : null)
                .categoryId(product != null ? product.getCategoryId() : null)
                .categoryName(product != null ? product.getCategoryName() : null)
                .userId(favoriteProduct.getUserId())
                .build();
    }
}

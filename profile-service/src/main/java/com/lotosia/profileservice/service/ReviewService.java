package com.lotosia.profileservice.service;

import com.lotosia.profileservice.client.OrderClient;
import com.lotosia.profileservice.client.ProductClient;
import com.lotosia.profileservice.dto.favoriteproduct.ProductResponse;
import com.lotosia.profileservice.dto.review.ReviewRequest;
import com.lotosia.profileservice.entity.Review;
import com.lotosia.profileservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final OrderClient orderClient;
    private final ProductClient productClient;
    private final ReviewRepository reviewRepository;

    public Review addReview(Long userId, ReviewRequest request) {
        if (request.getComment() == null || request.getComment().isBlank()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        ProductResponse product;
        try {
            product = productClient.getProductById(request.getProductId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Product not found with id: " + request.getProductId());
        }

        boolean delivered = orderClient.hasUserReceivedProduct(userId, request.getProductId());
        if (!delivered) {
            throw new IllegalStateException("You can only review products that have been delivered");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(request.getProductId());
        review.setComment(request.getComment());
        review.setRating(request.getRating());
        review.setCreatedDate(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }
}

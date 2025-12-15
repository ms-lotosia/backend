package com.lotosia.profileservice.controller;

import com.lotosia.profileservice.dto.review.ReviewRequest;
import com.lotosia.profileservice.entity.Review;
import com.lotosia.profileservice.service.ReviewService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("create")
    public ResponseEntity<Review> addReview(@RequestBody ReviewRequest reviewRequest) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getDetails();

        Review review = reviewService.addReview(userId, reviewRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    @GetMapping("/user")
    public ResponseEntity<List<Review>> getReviewsByUser() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getDetails();
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }
}

package com.lotosia.profileservice.service;

import com.lotosia.profileservice.client.ProductClient;
import com.lotosia.profileservice.client.ShoppingClient;
import com.lotosia.profileservice.dto.review.ReviewRequest;
import com.lotosia.profileservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author: nijataghayev
 */

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ShoppingClient shoppingClient;
    private final ProductClient productClient;
    private final ReviewRepository reviewRepository;

    public ReviewResponse create(ReviewRequest request) {

    }
}

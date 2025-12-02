package com.lotosia.profileservice.repository;

import com.lotosia.profileservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: nijataghayev
 */

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    List<Review> findAllByProductId(Long productId);

    List<Review> findAllByUserId(Long userId);
}

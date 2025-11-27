package com.lotosia.paymentservice.repository;

import com.lotosia.paymentservice.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author: nijataghayev
 */

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByUserIdAndIsActiveTrue(Long userId);

    Optional<Card> findByUserIdAndIdAndIsActiveTrue(Long userId, Long cardId);

    Optional<Card> findByUserIdAndIsDefaultTrueAndIsActiveTrue(Long userId);

    boolean existsByUserIdAndCardNumberAndIsActiveTrue(Long userId, String cardNumber);
}



package com.lotosia.shoppingservice.repository;

import com.lotosia.shoppingservice.entity.Order;
import com.lotosia.shoppingservice.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByUserIdAndItems_ProductIdAndStatus(Long userId, Long productId, OrderStatus status);
}

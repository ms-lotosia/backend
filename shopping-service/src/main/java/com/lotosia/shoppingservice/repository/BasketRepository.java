package com.lotosia.shoppingservice.repository;

import com.lotosia.shoppingservice.entity.Basket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author: nijataghayev
 */

@Repository
public interface BasketRepository extends JpaRepository<Basket, Integer> {
}

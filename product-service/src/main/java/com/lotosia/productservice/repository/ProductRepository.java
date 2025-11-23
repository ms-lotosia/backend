package com.lotosia.productservice.repository;

import com.lotosia.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: nijataghayev
 */

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);

    boolean existsByName(String name);
}


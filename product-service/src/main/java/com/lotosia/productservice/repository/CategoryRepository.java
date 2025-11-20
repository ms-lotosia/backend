package com.lotosia.productservice.repository;

import com.lotosia.productservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author: nijataghayev
 */

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}

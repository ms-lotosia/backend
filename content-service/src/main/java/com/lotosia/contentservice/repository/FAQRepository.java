package com.lotosia.contentservice.repository;

import com.lotosia.contentservice.entity.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author: nijataghayev
 */

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Long> {

}

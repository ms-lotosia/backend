package com.lotosia.supportservice.repository;

import com.lotosia.supportservice.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author: nijataghayev
 */

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
}

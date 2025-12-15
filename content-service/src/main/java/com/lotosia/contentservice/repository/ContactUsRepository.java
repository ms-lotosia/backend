package com.lotosia.contentservice.repository;

import com.lotosia.contentservice.entity.ContactUs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ContactUsRepository extends JpaRepository<ContactUs, Long> {
}

package com.lotosia.supportservice.repository;

import com.lotosia.supportservice.entity.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
}

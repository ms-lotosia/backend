package com.lotosia.profileservice.repository;

import com.lotosia.profileservice.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    Optional<UserPreference> findByProfileId(Long profileId);

    void deleteByProfileId(Long profileId);
}


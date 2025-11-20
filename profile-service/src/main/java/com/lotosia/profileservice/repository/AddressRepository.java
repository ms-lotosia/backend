package com.lotosia.profileservice.repository;

import com.lotosia.profileservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author: nijataghayev
 */

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByProfileId(Long profileId);

    Optional<Address> findByIdAndProfileId(Long id, Long profileId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.profile.id = :profileId")
    void clearDefaultAddress(Long profileId);
}

package com.lotosia.profileservice.repository;

import com.lotosia.profileservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author: nijataghayev
 */

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}

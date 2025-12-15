package com.lotosia.identityservice.repository;

import com.lotosia.identityservice.entity.Role;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RoleRepository {

    Optional<Role> findByName(String name);
}

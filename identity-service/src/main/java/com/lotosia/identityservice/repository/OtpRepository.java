package com.lotosia.identityservice.repository;

import com.lotosia.identityservice.entity.Otp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends MongoRepository<Otp, String> {

    Optional<Otp> findByEmail(String email);
}

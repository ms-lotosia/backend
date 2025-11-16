package com.lotosia.identityservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author: nijataghayev
 */

@Entity
@Table(name = "otp_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Otp extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "otp_code", nullable = false)
    private String otpCode;

    @Column(name = "expiration_time", nullable = false)
    private LocalDateTime expirationTime;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "hashed_password")
    private String hashedPassword;
}

package com.lotosia.identityservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "otp_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Otp {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String otpCode;

    @Indexed(expireAfterSeconds = 0)
    private LocalDateTime expirationTime;

    private String firstName;
    private String lastName;
    private String hashedPassword;

    private LocalDateTime createdAt;
}

package com.lotosia.profileservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileRequest {

    @NotNull
    private Long userId;

    @Size(max = 20)
    private String phoneNumber;

    @PastOrPresent
    private LocalDate birthDate;

    @Size(max = 255)
    private String profileImageUrl;
}


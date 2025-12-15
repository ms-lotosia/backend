package com.lotosia.identityservice.dto;

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

    private Long userId;
    private String phoneNumber;
    private LocalDate birthDate;
    private String profileImageUrl;
}

package com.lotosia.profileservice.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAddressRequest {

    @NotNull
    private Long profileId;

    @Size(min = 3, max = 50)
    @NotBlank
    private String firstName;

    @Size(min = 3, max = 70)
    @NotBlank
    private String lastName;

    @Size(max = 20)
    private String phoneNumber;

    @NotBlank
    private String addressLine1;

    private String addressLine2;

    @Size(max = 20)
    @NotBlank
    private String city;

    @Size(max = 40)
    @NotBlank
    private String state;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 40)
    @NotBlank
    private String country;

    private Boolean isDefault = false;
}

package com.lotosia.supportservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: nijataghayev
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintRequest {

    @NotBlank(message = "Email is not blank")
    @Email
    @Size(max = 70)
    private String email;

    @NotBlank
    @Size(max = 1000)
    private String message;
}

package com.lotosia.supportservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionRequest {

    @Schema(description = "User's email address to send a reply", example = "user@gmail.com")
    @Email(message = "Email format is incorrect")
    @NotBlank(message = "Email must not be blank")
    @Size(max = 70)
    private String email;

    @Schema(description = "The suggestion or message provided by the user", example = "It would be great to have a dark mode.")
    @NotBlank(message = "Message must not be blank")
    @Size(max = 1000)
    private String message;
}

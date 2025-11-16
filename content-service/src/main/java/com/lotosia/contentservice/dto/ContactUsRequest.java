package com.lotosia.contentservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class ContactUsRequest {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 50)
    private String email;

    @NotBlank(message = "Contact us first name cannot blank")
    @Size(max = 50)
    @Pattern(regexp = "^[a-zA-ZəƏıİöÖüÜçÇşŞğĞ ]*$",
            message = "Quote author can only contain letters and spaces")
    private String firstName;

    @NotBlank(message = "Contact us last name cannot blank")
    @Size(max = 75)
    @Pattern(regexp = "^[a-zA-ZəƏıİöÖüÜçÇşŞğĞ ]*$",
            message = "Quote author can only contain letters and spaces")
    private String lastName;

    @NotBlank(message = "Contact us message cannot be blank")
    @Size(max = 1000)
    private String message;

    @NotBlank(message = "Contact us subject cannot be blank")
    @Size(min = 5, max = 50)
    private String subject;
}

package com.lotosia.contentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FAQRequest {

    @Schema(description = "The text of the frequently asked question", example = "How can I delete my account?")
    private String question;

    @Schema(description = "The answer to the frequently asked question", example = "Go to your profile settings and select 'Delete Account'")
    private String answer;
}

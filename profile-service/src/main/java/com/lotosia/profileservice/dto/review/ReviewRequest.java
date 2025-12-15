package com.lotosia.profileservice.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    @NotNull
    private Long productId;

    @NotBlank
    @Size(min = 1, max = 500)
    private String comment;

    @NotNull
    @Min(value = 1)
    @Max(value = 5)
    private Integer rating;
}

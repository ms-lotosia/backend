package com.lotosia.productservice.dto.category;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategory {

    @NotNull
    @Size(min = 1, max = 50)
    private String name;

    private String description;
    private MultipartFile image;
}

package com.lotosia.productservice.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategory {

    private Long id;
    private String name;
    private String description;
    private MultipartFile image;
}

package com.lotosia.productservice.controller;

import com.lotosia.productservice.dto.category.CategoryResponse;
import com.lotosia.productservice.dto.category.CreateCategory;
import com.lotosia.productservice.dto.category.UpdateCategory;
import com.lotosia.productservice.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category Controller")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/create")
    public ResponseEntity<CategoryResponse> create(CreateCategory request) {
        return ResponseEntity.ok(categoryService.create(request));
    }

    @PutMapping("/update")
    public ResponseEntity<CategoryResponse> update(UpdateCategory request) {
        return ResponseEntity.ok(categoryService.update(request));
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CategoryResponse>> findAll() {
        List<CategoryResponse> responses = categoryService.findAll();

        if (responses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(responses);
    }

    @GetMapping("{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }
}

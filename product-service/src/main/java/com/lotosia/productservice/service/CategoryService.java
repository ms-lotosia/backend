package com.lotosia.productservice.service;

import com.lotosia.productservice.dto.category.CategoryResponse;
import com.lotosia.productservice.dto.category.CreateCategory;
import com.lotosia.productservice.dto.category.UpdateCategory;
import com.lotosia.productservice.entity.Category;
import com.lotosia.productservice.exception.ResourceNotFoundException;
import com.lotosia.productservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: nijataghayev
 */

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryResponse create(CreateCategory request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category savedCategory = categoryRepository.save(category);
        return mapToDto(savedCategory);
    }

    public CategoryResponse update(UpdateCategory request) {
        Category category = categoryRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getName() != null) {
            category.setName(request.getName());
        }

        if (category.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        Category savedCategory = categoryRepository.save(category);
        return mapToDto(savedCategory);
    }

    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found for category id " + id));

        return mapToDto(category);
    }

    public List<CategoryResponse> findAll() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> dtos = categories.stream()
                .map(this::mapToDto)
                .toList();

        return dtos;
    }

    private CategoryResponse mapToDto(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getDescription()
        );
    }
}

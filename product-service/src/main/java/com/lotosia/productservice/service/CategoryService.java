package com.lotosia.productservice.service;

import com.lotosia.productservice.dto.category.CategoryResponse;
import com.lotosia.productservice.dto.category.CreateCategory;
import com.lotosia.productservice.dto.category.UpdateCategory;
import com.lotosia.productservice.entity.Category;
import com.lotosia.productservice.exception.AlreadyExistsException;
import com.lotosia.productservice.exception.ResourceNotFoundException;
import com.lotosia.productservice.repository.CategoryRepository;
import com.lotosia.productservice.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    public CategoryResponse create(CreateCategory request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new AlreadyExistsException("Category already exists: " + request.getName());
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imageUrl = fileStorageService.saveFile(request.getImage(), "/categories");
            category.setImage(imageUrl);
        }

        Category savedCategory = categoryRepository.save(category);
        return mapToDto(savedCategory);
    }

    public CategoryResponse update(UpdateCategory request) {
        Category category = categoryRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (request.getName() != null) {
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imageUrl = fileStorageService.saveFile(request.getImage(), "/categories");
            category.setImage(imageUrl);
        }

        Category savedCategory = categoryRepository.save(category);
        return mapToDto(savedCategory);
    }

    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

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
                c.getDescription(),
                c.getImage()
        );
    }
}

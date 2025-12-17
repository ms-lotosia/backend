package com.lotosia.productservice.service;

import com.lotosia.productservice.criteria.PageCriteria;
import com.lotosia.productservice.dto.PageableResponse;
import com.lotosia.productservice.dto.product.CreateProduct;
import com.lotosia.productservice.dto.product.ProductResponse;
import com.lotosia.productservice.dto.product.UpdateProduct;
import com.lotosia.productservice.entity.Category;
import com.lotosia.productservice.entity.Product;
import com.lotosia.productservice.exception.AlreadyExistsException;
import com.lotosia.productservice.exception.ResourceNotFoundException;
import com.lotosia.productservice.repository.CategoryRepository;
import com.lotosia.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    public PageableResponse<ProductResponse> getAllProducts(PageCriteria pageCriteria) {
        PageRequest pageable = PageRequest.of(pageCriteria.getPage(),
                pageCriteria.getCount(),
                Sort.by("id").ascending());

        Page<Product> products = productRepository.findAll(pageable);

        return toPageableProductResponse(products);
    }

    public ProductResponse getProductById(Long id) {
        if (id == null || id <= 0) {
            throw new ResourceNotFoundException("Product ID must be positive. Provided: " + id);
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for id: " + id));

        return mapToDto(product);
    }

    public List<ProductResponse> getProductsByCategoryId(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new ResourceNotFoundException("Category ID must be positive. Provided: " + categoryId);
        }

        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products.stream()
                .map(this::mapToDto)
                .toList();
    }

    public ProductResponse createProduct(CreateProduct request) {
        if (productRepository.existsByName(request.getName())) {
            throw new AlreadyExistsException("Product already exists: " + request.getName());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found for id: " + request.getCategoryId()));

        
        List<String> imageUrls = fileStorageService.saveFiles(request.getImages());

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImages(imageUrls);
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    public ProductResponse updateProduct(UpdateProduct request) {
        Product product = productRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for id: " + request.getId()));

        if (request.getName() != null && !request.getName().isEmpty()) {
            
            if (productRepository.existsByName(request.getName()) && 
                !product.getName().equals(request.getName())) {
                throw new AlreadyExistsException("Product already exists: " + request.getName());
            }
            product.setName(request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        if (request.getImages() != null) {
            product.setImages(request.getImages());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found for id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for id: " + id));

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            try {
                fileStorageService.deleteFiles(product.getImages());
            } catch (Exception ignored) {
            }
        }

        productRepository.delete(product);
    }

    private PageableResponse<ProductResponse> toPageableProductResponse(Page<Product> products) {
        PageableResponse<ProductResponse> pageableResponse = new PageableResponse<>();
        pageableResponse.setTotalElements(products.getTotalElements());
        pageableResponse.setData(products.stream().map(this::mapToDto).toList());
        pageableResponse.setHasNextPage(products.hasNext());
        pageableResponse.setLastPageNumber(products.getNumber());

        return pageableResponse;
    }

    private ProductResponse mapToDto(Product product) {
        ProductResponse dto = new ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImages(product.getImages());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        return dto;
    }
}

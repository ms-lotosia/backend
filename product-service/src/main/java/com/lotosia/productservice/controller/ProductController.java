package com.lotosia.productservice.controller;

import com.lotosia.productservice.criteria.PageCriteria;
import com.lotosia.productservice.dto.PageableResponse;
import com.lotosia.productservice.dto.ResponseModel;
import com.lotosia.productservice.dto.product.CreateProduct;
import com.lotosia.productservice.dto.product.ProductResponse;
import com.lotosia.productservice.dto.product.UpdateProduct;
import com.lotosia.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Controller")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Get all products")
    @GetMapping("/all")
    @ResponseStatus(OK)
    public ResponseModel<PageableResponse<ProductResponse>> getAll(@ModelAttribute PageCriteria pageCriteria) {
        PageableResponse<ProductResponse> response = productService.getAllProducts(pageCriteria);
        return ResponseModel.<PageableResponse<ProductResponse>>builder()
                .data(response)
                .build();
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    @ResponseStatus(OK)
    public ResponseModel<ProductResponse> getById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseModel.<ProductResponse>builder()
                .data(response)
                .build();
    }

    @Operation(summary = "Get products by category ID")
    @GetMapping("/category/{categoryId}")
    @ResponseStatus(OK)
    public ResponseModel<List<ProductResponse>> getByCategoryId(@PathVariable Long categoryId) {
        List<ProductResponse> response = productService.getProductsByCategoryId(categoryId);
        return ResponseModel.<List<ProductResponse>>builder()
                .data(response)
                .build();
    }

    @Operation(summary = "Create a new product")
    @PostMapping(value = "/create", consumes = "multipart/form-data")
    @ResponseStatus(CREATED)
    public ResponseModel<ProductResponse> create(@ModelAttribute @Valid CreateProduct request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseModel.<ProductResponse>builder()
                .data(response)
                .build();
    }

    @Operation(summary = "Update product by ID")
    @PutMapping("/update")
    @ResponseStatus(OK)
    public ResponseModel<ProductResponse> update(@RequestBody @Valid UpdateProduct request) {
        ProductResponse response = productService.updateProduct(request);
        return ResponseModel.<ProductResponse>builder()
                .data(response)
                .build();
    }

    @Operation(summary = "Delete product by ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(OK)
    public void delete(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}

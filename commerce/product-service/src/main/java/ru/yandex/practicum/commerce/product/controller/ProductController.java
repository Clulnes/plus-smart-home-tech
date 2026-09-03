package ru.yandex.practicum.commerce.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.product.dto.CreateProductRequest;
import ru.yandex.practicum.commerce.product.dto.ProductDto;
import ru.yandex.practicum.commerce.product.dto.UpdateProductRequest;
import ru.yandex.practicum.commerce.product.service.CatalogService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final CatalogService catalogService;

    @GetMapping
    public List<ProductDto> getProducts() {
        return catalogService.retrieveActiveProducts();
    }

    @GetMapping("/{id}")
    public ProductDto getProductById(@PathVariable Long id) {
        return catalogService.retrieveProductById(id);
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductDto> getProductsByCategory(@PathVariable Long categoryId) {
        return catalogService.retrieveProductsByCategory(categoryId);
    }

    @GetMapping("/search")
    public List<ProductDto> searchProducts(@RequestParam(name = "query", required = false) String query) {
        return catalogService.searchProducts(query);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto createProduct(@Valid @RequestBody CreateProductRequest request) {
        return catalogService.saveProduct(request);
    }

    @PatchMapping("/{id}")
    public ProductDto updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return catalogService.modifyProduct(id, request);
    }
}
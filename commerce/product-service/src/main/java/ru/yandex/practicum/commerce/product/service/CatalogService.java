package ru.yandex.practicum.commerce.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.product.dto.*;
import ru.yandex.practicum.commerce.product.exception.NotFoundException;
import ru.yandex.practicum.commerce.product.model.Category;
import ru.yandex.practicum.commerce.product.model.Product;
import ru.yandex.practicum.commerce.product.repository.CategoryRepository;
import ru.yandex.practicum.commerce.product.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<CategoryDto> retrieveAllCategories() {
        return categoryRepository.findAll().stream().map(this::mapCategoryToDto).toList();
    }

    public CategoryDto retrieveCategoryById(Long id) {
        return categoryRepository.findById(id).map(this::mapCategoryToDto)
                .orElseThrow(() -> new NotFoundException("Category with ID " + id + " was not found"));
    }

    @Transactional
    public CategoryDto saveCategory(CreateCategoryRequest req) {
        Category entity = new Category();
        entity.setName(req.name());
        entity.setDescription(req.description());
        return mapCategoryToDto(categoryRepository.save(entity));
    }

    public List<ProductDto> retrieveActiveProducts() {
        return productRepository.findAllByActiveTrue().stream().map(this::mapProductToDto).toList();
    }

    public ProductDto retrieveProductById(Long id) {
        return productRepository.findById(id).map(this::mapProductToDto)
                .orElseThrow(() -> new NotFoundException("Product with ID " + id + " was not found"));
    }

    public List<ProductDto> retrieveProductsByCategory(Long categoryId) {
        return productRepository.findAllByCategoryIdAndActiveTrue(categoryId).stream().map(this::mapProductToDto).toList();
    }

    public List<ProductDto> searchProducts(String text) {
        if (text == null || text.isBlank()) {
            return retrieveActiveProducts();
        }
        return productRepository.findAllByNameContainingIgnoreCaseAndActiveTrue(text).stream().map(this::mapProductToDto).toList();
    }

    @Transactional
    public ProductDto saveProduct(CreateProductRequest req) {
        Category category = null;
        if (req.categoryId() != null) {
            category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
        }
        Product entity = new Product();
        entity.setName(req.name());
        entity.setDescription(req.description());
        entity.setPrice(req.price());
        entity.setCategory(category);
        entity.setImageUrl(req.imageUrl());
        entity.setActive(true);
        return mapProductToDto(productRepository.save(entity));
    }

    @Transactional
    public ProductDto modifyProduct(Long id, UpdateProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (req.name() != null) product.setName(req.name());
        if (req.description() != null) product.setDescription(req.description());
        if (req.price() != null) product.setPrice(req.price());
        if (req.imageUrl() != null) product.setImageUrl(req.imageUrl());
        if (req.active() != null) product.setActive(req.active());
        if (req.categoryId() != null) {
            Category cat = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            product.setCategory(cat);
        }
        return mapProductToDto(productRepository.save(product));
    }

    public CategoryDto mapCategoryToDto(Category c) {
        if (c == null) return null;
        return new CategoryDto(c.getId(), c.getName(), c.getDescription());
    }

    public ProductDto mapProductToDto(Product p) {
        if (p == null) return null;
        return new ProductDto(p.getId(), p.getName(), p.getDescription(), p.getPrice(),
                mapCategoryToDto(p.getCategory()), p.getImageUrl(), p.getActive());
    }
}
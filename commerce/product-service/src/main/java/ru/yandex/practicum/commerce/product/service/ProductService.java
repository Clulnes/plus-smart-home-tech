package ru.yandex.practicum.commerce.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.product.dto.CreateProductRequest;
import ru.yandex.practicum.commerce.product.dto.ProductDto;
import ru.yandex.practicum.commerce.product.dto.UpdateProductRequest;
import ru.yandex.practicum.commerce.product.exception.NotFoundException;
import ru.yandex.practicum.commerce.product.model.Category;
import ru.yandex.practicum.commerce.product.model.Product;
import ru.yandex.practicum.commerce.product.repository.CategoryRepository;
import ru.yandex.practicum.commerce.product.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    public List<ProductDto> getAllProducts() {
        return productRepository.findAllByActiveTrue().stream()
                .map(this::toDto)
                .toList();
    }

    public ProductDto getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Товар с id=" + id + " не найден"));
    }

    public List<ProductDto> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<ProductDto> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return getAllProducts();
        }
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(query).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();

        return toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Товар не найден"));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) product.setActive(request.getActive());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            product.setCategory(category);
        }

        return toDto(productRepository.save(product));
    }

    public ProductDto toDto(Product product) {
        if (product == null) return null;
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(categoryService.toDto(product.getCategory()))
                .imageUrl(product.getImageUrl())
                .active(product.getActive())
                .build();
    }
}
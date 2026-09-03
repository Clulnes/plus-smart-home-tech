package ru.yandex.practicum.commerce.product.dto;

import java.math.BigDecimal;

public record ProductDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        CategoryDto category,
        String imageUrl,
        Boolean active
) {}
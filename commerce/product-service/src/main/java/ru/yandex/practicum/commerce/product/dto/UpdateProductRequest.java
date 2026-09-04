package ru.yandex.practicum.commerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @Size(max = 255)
        String name,

        @Size(max = 2000)
        String description,

        @DecimalMin(value = "0.01")
        BigDecimal price,

        Long categoryId,
        String imageUrl,
        Boolean active
) {}
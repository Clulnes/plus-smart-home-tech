package ru.yandex.practicum.commerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Product title cannot be empty")
        @Size(max = 255)
        String name,

        @Size(max = 2000)
        String description,

        @NotNull(message = "Product price must be specified")
        @DecimalMin(value = "0.01")
        BigDecimal price,

        Long categoryId,
        String imageUrl
) {}
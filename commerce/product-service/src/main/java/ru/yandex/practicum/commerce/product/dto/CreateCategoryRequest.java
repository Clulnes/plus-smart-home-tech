package ru.yandex.practicum.commerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "Category title is required")
        @Size(max = 255)
        String name,

        @Size(max = 500)
        String description
) {}
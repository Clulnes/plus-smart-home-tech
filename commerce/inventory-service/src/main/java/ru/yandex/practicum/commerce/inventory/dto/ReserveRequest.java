package ru.yandex.practicum.commerce.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReserveRequest(
        @NotNull(message = "productId must not be null")
        Long productId,

        @NotNull(message = "quantity must not be null")
        @Min(value = 1, message = "quantity must be at least 1")
        Integer quantity
) {}
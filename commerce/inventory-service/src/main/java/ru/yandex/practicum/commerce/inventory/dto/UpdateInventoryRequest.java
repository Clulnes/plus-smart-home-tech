package ru.yandex.practicum.commerce.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateInventoryRequest(
        @NotNull(message = "productId must not be null")
        Long productId,

        @NotNull(message = "quantity must not be null")
        @Min(value = 0, message = "quantity must be positive or zero")
        Integer quantity
) {}
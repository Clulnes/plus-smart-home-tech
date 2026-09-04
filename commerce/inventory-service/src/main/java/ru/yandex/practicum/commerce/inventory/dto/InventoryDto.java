package ru.yandex.practicum.commerce.inventory.dto;

public record InventoryDto(
        Long id,
        Long productId,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity
) {}
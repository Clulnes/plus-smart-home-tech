package ru.yandex.practicum.commerce.inventory.dto;

public record ReserveResponse(
        boolean success,
        Integer availableQuantity,
        String message
) {}
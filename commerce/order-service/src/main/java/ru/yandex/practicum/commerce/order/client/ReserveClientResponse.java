package ru.yandex.practicum.commerce.order.client;

public record ReserveClientResponse(
        boolean success,
        Integer availableQuantity,
        String message
) {}
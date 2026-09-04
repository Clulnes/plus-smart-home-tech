package ru.yandex.practicum.commerce.order.client;

public record ReserveClientRequest(
        Long productId,
        Integer quantity
) {}
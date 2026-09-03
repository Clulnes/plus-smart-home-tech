package ru.yandex.practicum.commerce.order.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal price
) {}
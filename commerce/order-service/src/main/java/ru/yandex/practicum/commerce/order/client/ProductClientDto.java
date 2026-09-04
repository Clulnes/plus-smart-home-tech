package ru.yandex.practicum.commerce.order.client;

import java.math.BigDecimal;

public record ProductClientDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Object category,
        String imageUrl,
        Boolean active
) {}
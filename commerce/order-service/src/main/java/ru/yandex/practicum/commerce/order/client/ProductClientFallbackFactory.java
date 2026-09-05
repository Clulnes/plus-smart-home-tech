package ru.yandex.practicum.commerce.order.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.order.exception.ProductServiceUnavailableException;

@Slf4j
@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {
    @Override
    public ProductClient create(Throwable cause) {
        return productId -> {
            log.warn("ProductClient fallback triggered for product ID={}: {}", productId, cause.getMessage());
            throw new ProductServiceUnavailableException(productId, cause);
        };
    }
}
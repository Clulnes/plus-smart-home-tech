package ru.yandex.practicum.commerce.order.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.order.exception.InventoryServiceUnavailableException;

@Slf4j
@Component
public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {
    @Override
    public InventoryClient create(Throwable cause) {
        return new InventoryClient() {
            @Override
            public ReserveClientResponse reserveStock(ReserveClientRequest request) {
                log.warn("InventoryClient reserve fallback triggered for product ID={}: {}", request.productId(), cause.getMessage());
                throw new InventoryServiceUnavailableException(request.productId(), cause);
            }

            @Override
            public ReserveClientResponse releaseStock(ReserveClientRequest request) {
                log.warn("InventoryClient release fallback triggered for product ID={}: {}", request.productId(), cause.getMessage());
                throw new InventoryServiceUnavailableException(request.productId(), cause);
            }
        };
    }
}
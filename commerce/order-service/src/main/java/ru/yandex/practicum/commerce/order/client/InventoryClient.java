package ru.yandex.practicum.commerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", path = "/api/inventory")
public interface InventoryClient {
    @PostMapping("/reserve")
    ReserveClientResponse reserveStock(@RequestBody ReserveClientRequest request);

    @PostMapping("/release")
    ReserveClientResponse releaseStock(@RequestBody ReserveClientRequest request);
}
package ru.yandex.practicum.commerce.inventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.inventory.dto.InventoryDto;
import ru.yandex.practicum.commerce.inventory.dto.ReserveRequest;
import ru.yandex.practicum.commerce.inventory.dto.ReserveResponse;
import ru.yandex.practicum.commerce.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.commerce.inventory.service.InventoryService;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService InventoryService;

    @GetMapping
    public List<InventoryDto> getAll() {
        return InventoryService.fetchAllStocks();
    }

    @GetMapping("/{productId}")
    public InventoryDto getByProduct(@PathVariable Long productId) {
        return InventoryService.fetchByProductId(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryDto create(@Valid @RequestBody UpdateInventoryRequest request) {
        return InventoryService.registerStock(request);
    }

    @PutMapping
    public InventoryDto update(@Valid @RequestBody UpdateInventoryRequest request) {
        return InventoryService.modifyStockQuantity(request);
    }

    @PostMapping("/reserve")
    public ReserveResponse reserve(@Valid @RequestBody ReserveRequest request) {
        return InventoryService.executeReservation(request);
    }

    @PostMapping("/release")
    public ReserveResponse release(@Valid @RequestBody ReserveRequest request) {
        return InventoryService.releaseReservation(request);
    }
}
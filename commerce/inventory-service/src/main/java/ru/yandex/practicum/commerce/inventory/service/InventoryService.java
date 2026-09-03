package ru.yandex.practicum.commerce.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.inventory.dto.InventoryDto;
import ru.yandex.practicum.commerce.inventory.dto.ReserveRequest;
import ru.yandex.practicum.commerce.inventory.dto.ReserveResponse;
import ru.yandex.practicum.commerce.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.commerce.inventory.exception.ConflictException;
import ru.yandex.practicum.commerce.inventory.exception.NotFoundException;
import ru.yandex.practicum.commerce.inventory.model.Inventory;
import ru.yandex.practicum.commerce.inventory.repository.InventoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public List<InventoryDto> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public InventoryDto getByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Остатки для товара с productId=" + productId + " не найдены"));
    }

    @Transactional
    public InventoryDto createInventory(UpdateInventoryRequest request) {
        if (inventoryRepository.existsByProductId(request.getProductId())) {
            throw new ConflictException("Запись об остатках для товара с productId=" + request.getProductId() + " уже существует");
        }

        Inventory inventory = Inventory.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .reservedQuantity(0)
                .build();

        return toDto(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryDto updateInventory(UpdateInventoryRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Остатки для товара с productId=" + request.getProductId() + " не найдены"));

        inventory.setQuantity(request.getQuantity());
        return toDto(inventoryRepository.save(inventory));
    }

    @Transactional
    public ReserveResponse reserveStock(ReserveRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Товар с productId=" + request.getProductId() + " не найден на складе"));

        if (inventory.getAvailableQuantity() < request.getQuantity()) {
            throw new ConflictException("Недостаточно товара на складе. Доступно: " + inventory.getAvailableQuantity() + ", запрошено: " + request.getQuantity());
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.getQuantity());
        inventoryRepository.save(inventory);

        return ReserveResponse.builder()
                .success(true)
                .availableQuantity(inventory.getAvailableQuantity())
                .message("Товар успешно зарезервирован")
                .build();
    }

    public InventoryDto toDto(Inventory inventory) {
        if (inventory == null) return null;
        return InventoryDto.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .build();
    }
}
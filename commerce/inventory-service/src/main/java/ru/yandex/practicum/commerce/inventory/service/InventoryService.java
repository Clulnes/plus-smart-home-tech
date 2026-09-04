package ru.yandex.practicum.commerce.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.inventory.dto.InventoryDto;
import ru.yandex.practicum.commerce.inventory.dto.ReserveRequest;
import ru.yandex.practicum.commerce.inventory.dto.ReserveResponse;
import ru.yandex.practicum.commerce.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.commerce.inventory.exception.NotFoundException;
import ru.yandex.practicum.commerce.inventory.exception.ConflictException;
import ru.yandex.practicum.commerce.inventory.model.Inventory;
import ru.yandex.practicum.commerce.inventory.repository.InventoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {
    private final InventoryRepository InventoryRepository;

    public List<InventoryDto> fetchAllStocks() {
        return InventoryRepository.findAll().stream().map(this::toDto).toList();
    }

    public InventoryDto fetchByProductId(Long productId) {
        return InventoryRepository.findByProductId(productId).map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Stock record for product ID " + productId + " not found"));
    }

    @Transactional
    public InventoryDto registerStock(UpdateInventoryRequest req) {
        if (InventoryRepository.existsByProductId(req.productId())) {
            throw new IllegalArgumentException("Record for product " + req.productId() + " already exists");
        }
        Inventory item = new Inventory();
        item.setProductId(req.productId());
        item.setQuantity(req.quantity());
        item.setReservedQuantity(0);
        item.refreshAvailable();
        return toDto(InventoryRepository.save(item));
    }

    @Transactional
    public InventoryDto modifyStockQuantity(UpdateInventoryRequest req) {
        Inventory item = InventoryRepository.findByProductId(req.productId())
                .orElseThrow(() -> new NotFoundException("Stock record not found"));

        if (req.quantity() < item.getReservedQuantity()) {
            throw new ConflictException("New quantity cannot be lower than current reserved quantity");
        }
        item.setQuantity(req.quantity());
        item.refreshAvailable();
        return toDto(InventoryRepository.save(item));
    }

    @Transactional
    public ReserveResponse executeReservation(ReserveRequest req) {
        Inventory item = InventoryRepository.findByProductId(req.productId())
                .orElseThrow(() -> new NotFoundException("Stock item not found for product ID " + req.productId()));

        if (req.quantity() > item.getAvailableQuantity()) {
            throw new ConflictException("Not enough available stock to reserve");
        }

        item.setReservedQuantity(item.getReservedQuantity() + req.quantity());
        item.refreshAvailable();
        InventoryRepository.save(item);

        return new ReserveResponse(true, item.getAvailableQuantity(), "Reservation successful");
    }

    @Transactional
    public ReserveResponse releaseReservation(ReserveRequest req) {
        Inventory item = InventoryRepository.findByProductId(req.productId())
                .orElseThrow(() -> new NotFoundException("Stock item not found for product ID " + req.productId()));

        if (req.quantity() > item.getReservedQuantity()) {
            throw new IllegalArgumentException("Cannot release more than currently reserved");
        }

        item.setReservedQuantity(item.getReservedQuantity() - req.quantity());
        item.refreshAvailable();
        InventoryRepository.save(item);

        return new ReserveResponse(true, item.getAvailableQuantity(), "Reservation released successfully");
    }

    private InventoryDto toDto(Inventory item) {
        return new InventoryDto(item.getId(), item.getProductId(), item.getQuantity(), item.getReservedQuantity(),
                item.getAvailableQuantity());
    }
}
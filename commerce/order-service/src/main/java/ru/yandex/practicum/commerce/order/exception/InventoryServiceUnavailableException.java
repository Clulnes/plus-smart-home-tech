package ru.yandex.practicum.commerce.order.exception;

public class InventoryServiceUnavailableException extends RuntimeException {
    public InventoryServiceUnavailableException(Long productId, Throwable cause) {
        super("Inventory service is temporarily unavailable for product ID: " + productId, cause);
    }
}
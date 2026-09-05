package ru.yandex.practicum.commerce.order.exception;

public class ProductServiceUnavailableException extends RuntimeException {
    public ProductServiceUnavailableException(Long productId, Throwable cause) {
        super("Product service is temporarily unavailable for product ID: " + productId, cause);
    }
}
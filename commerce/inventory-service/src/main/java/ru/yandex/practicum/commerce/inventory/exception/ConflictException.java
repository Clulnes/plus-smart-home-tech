package ru.yandex.practicum.commerce.inventory.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
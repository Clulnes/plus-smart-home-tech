package ru.yandex.practicum.commerce.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.order.dto.CreateOrderRequest;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.order.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public List<OrderDto> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public OrderDto getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/by-email")
    public List<OrderDto> getOrdersByEmail(@RequestParam String email) {
        return orderService.getOrdersByEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }
}
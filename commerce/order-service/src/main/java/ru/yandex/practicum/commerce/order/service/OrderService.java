package ru.yandex.practicum.commerce.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.order.dto.CreateOrderRequest;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.order.dto.OrderItemDto;
import ru.yandex.practicum.commerce.order.exception.NotFoundException;
import ru.yandex.practicum.commerce.order.model.Order;
import ru.yandex.practicum.commerce.order.model.OrderItem;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public OrderDto getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Заказ с id=" + id + " не найден"));
    }

    public List<OrderDto> getOrdersByEmail(String email) {
        return orderRepository.findByCustomerEmailIgnoreCase(email).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        BigDecimal totalPrice = request.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .status("CREATED")
                .totalPrice(totalPrice)
                .statusDetails("Заказ успешно оформлен")
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemReq -> OrderItem.builder()
                        .productId(itemReq.getProductId())
                        .productName(itemReq.getProductName())
                        .quantity(itemReq.getQuantity())
                        .price(itemReq.getPrice())
                        .order(order)
                        .build())
                .toList();

        order.getItems().addAll(orderItems);

        return toDto(orderRepository.save(order));
    }

    public OrderDto toDto(Order order) {
        if (order == null) return null;

        List<OrderItemDto> itemDtos = order.getItems() != null
                ? order.getItems().stream().map(this::toItemDto).toList()
                : List.of();

        return OrderDto.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .statusDetails(order.getStatusDetails())
                .createdAt(order.getCreatedAt())
                .items(itemDtos)
                .build();
    }

    private OrderItemDto toItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }
}
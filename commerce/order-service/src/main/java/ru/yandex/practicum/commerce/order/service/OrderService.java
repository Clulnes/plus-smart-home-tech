package ru.yandex.practicum.commerce.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.order.dto.*;
import ru.yandex.practicum.commerce.order.exception.NotFoundException;
import ru.yandex.practicum.commerce.order.model.OrderItem;
import ru.yandex.practicum.commerce.order.model.Order;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;

    public List<OrderDto> fetchAllOrders() {
        return orderRepository.findAll().stream().map(this::toDto).toList();
    }

    public OrderDto fetchOrderById(Long id) {
        return orderRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Order #" + id + " not found"));
    }

    public List<OrderDto> fetchOrdersByEmail(String email) {
        return orderRepository.findAllByCustomerEmailIgnoreCase(email).stream().map(this::toDto).toList();
    }

    @Transactional
    public OrderDto registerNewOrder(CreateOrderRequest req) {
        BigDecimal total = req.items().stream()
                .map(i -> i.price().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setCustomerName(req.customerName());
        order.setCustomerEmail(req.customerEmail());
        order.setStatus("CREATED");
        order.setTotalPrice(total);
        order.setStatusDetails("Order accepted");
        order.setCreatedAt(LocalDateTime.now());

        for (OrderItemRequest itemReq : req.items()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemReq.productId());
            item.setProductName(itemReq.productName());
            item.setQuantity(itemReq.quantity());
            item.setPrice(itemReq.price());
            order.bindItem(item);
        }

        return toDto(orderRepository.save(order));
    }

    private OrderDto toDto(Order o) {
        List<OrderItemDto> itemDtos = o.getItems().stream()
                .map(i -> new OrderItemDto(i.getId(), i.getProductId(), i.getProductName(), i.getQuantity(), i.getPrice()))
                .toList();

        return new OrderDto(o.getId(), o.getCustomerName(), o.getCustomerEmail(), o.getStatus(),
                o.getTotalPrice(), o.getStatusDetails(), o.getCreatedAt(), itemDtos);
    }
}
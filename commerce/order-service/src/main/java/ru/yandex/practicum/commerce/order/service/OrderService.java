package ru.yandex.practicum.commerce.order.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.order.client.*;
import ru.yandex.practicum.commerce.order.dto.*;
import ru.yandex.practicum.commerce.order.exception.InventoryServiceUnavailableException;
import ru.yandex.practicum.commerce.order.exception.NotFoundException;
import ru.yandex.practicum.commerce.order.exception.OrderProcessingException;
import ru.yandex.practicum.commerce.order.exception.ProductServiceUnavailableException;
import ru.yandex.practicum.commerce.order.model.OrderItem;
import ru.yandex.practicum.commerce.order.model.Order;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    public List<OrderDto> fetchAllOrders() {
        return orderRepository.findAll().stream().map(this::toDto).toList();
    }

    public OrderDto fetchOrderById(Long id) {
        return orderRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Order #" + id + " not found"));
    }

    public List<OrderDto> fetchOrdersByEmail(String email) {
        if (email == null || email.isBlank()) {
            return fetchAllOrders();
        }
        return orderRepository.findAllByCustomerEmailIgnoreCase(email).stream().map(this::toDto).toList();
    }

    public OrderDto registerNewOrder(CreateOrderRequest req) {
        Map<Long, Integer> productQuantities = req.items().stream()
                .collect(Collectors.groupingBy(
                        OrderItemRequest::productId,
                        Collectors.summingInt(OrderItemRequest::quantity)
                ));

        Map<Long, ProductClientDto> productDataMap = new HashMap<>();
        boolean isDegraded = false;
        String degradationReason = null;

        for (Long productId : productQuantities.keySet()) {
            try {
                ProductClientDto product = productClient.getProductById(productId);
                if (product == null || Boolean.FALSE.equals(product.active())) {
                    throw new OrderProcessingException("Product is unavailable or inactive: " + productId);
                }
                productDataMap.put(productId, product);
            } catch (ProductServiceUnavailableException ex) {
                log.warn("Catalog unavailable, degraded mode activated for order");
                isDegraded = true;
                degradationReason = "Product service temporarily unreachable";
            } catch (FeignException.NotFound ex) {
                throw new OrderProcessingException("Product not found in catalog: " + productId);
            }
        }

        List<ReserveClientRequest> successfulReservations = new ArrayList<>();
        if (!isDegraded) {
            try {
                for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
                    ReserveClientRequest reserveReq = new ReserveClientRequest(entry.getKey(), entry.getValue());
                    ReserveClientResponse response = inventoryClient.reserveStock(reserveReq);
                    if (response == null || !response.success()) {
                        throw new OrderProcessingException("Insufficient stock for product ID: " + entry.getKey());
                    }
                    successfulReservations.add(reserveReq);
                }
            } catch (InventoryServiceUnavailableException ex) {
                log.warn("Inventory unavailable, degraded mode activated for order");
                isDegraded = true;
                degradationReason = "Inventory service temporarily unreachable";
            } catch (Exception ex) {
                log.warn("Reservation business failure, releasing {} items", successfulReservations.size());
                for (ReserveClientRequest rollback : successfulReservations) {
                    try {
                        inventoryClient.releaseStock(rollback);
                    } catch (Exception releaseEx) {
                        log.error("Failed to release reservation for product ID={}", rollback.productId(), releaseEx);
                    }
                }
                if (ex instanceof OrderProcessingException) {
                    throw (OrderProcessingException) ex;
                }
                throw new OrderProcessingException("Failed to reserve stock: " + ex.getMessage());
            }
        }

        return saveOrderInDb(req, productDataMap, isDegraded, degradationReason);
    }

    @Transactional
    public OrderDto saveOrderInDb(CreateOrderRequest req, Map<Long, ProductClientDto> productDataMap,
                                  boolean isDegraded, String degradationReason) {
        BigDecimal total = BigDecimal.ZERO;

        Order order = new Order();
        order.setCustomerName(req.customerName());
        order.setCustomerEmail(req.customerEmail());
        order.setStatus(isDegraded ? "PENDING_CONFIRMATION" : "CONFIRMED");
        order.setStatusDetails(isDegraded ? degradationReason : "Order confirmed and inventory reserved");
        order.setCreatedAt(LocalDateTime.now());

        for (OrderItemRequest itemReq : req.items()) {
            ProductClientDto product = productDataMap.get(itemReq.productId());
            BigDecimal price = (product != null && product.price() != null) ? product.price() : BigDecimal.ZERO;
            String name = (product != null && product.name() != null) ? product.name() : "Товар #"
                    + itemReq.productId() + " (ожидает проверки)";

            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(itemReq.quantity()));
            total = total.add(itemTotal);

            OrderItem item = new OrderItem();
            item.setProductId(itemReq.productId());
            item.setProductName(name);
            item.setQuantity(itemReq.quantity());
            item.setPrice(price);
            order.bindItem(item);
        }

        order.setTotalPrice(total);
        return toDto(orderRepository.save(order));
    }

    public OrderDto toDto(Order o) {
        if (o == null) return null;

        List<OrderItemDto> itemDtos = (o.getItems() != null)
                ? o.getItems().stream().map(i -> new OrderItemDto(i.getId(), i.getProductId(),
                i.getProductName(), i.getQuantity(), i.getPrice())).toList()
                : List.of();

        return new OrderDto(o.getId(), o.getCustomerName(), o.getCustomerEmail(), o.getStatus(),
                o.getTotalPrice(), o.getStatusDetails(), o.getCreatedAt(), itemDtos);
    }
}
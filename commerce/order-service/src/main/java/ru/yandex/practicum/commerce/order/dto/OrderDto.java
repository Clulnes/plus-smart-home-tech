package ru.yandex.practicum.commerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String customerName;
    private String customerEmail;
    private String status;
    private BigDecimal totalPrice;
    private String statusDetails;
    private LocalDateTime createdAt;
    private List<OrderItemDto> items;
}
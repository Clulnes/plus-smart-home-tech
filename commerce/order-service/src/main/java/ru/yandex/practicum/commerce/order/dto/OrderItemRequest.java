package ru.yandex.practicum.commerce.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    @NotNull
    private Long productId;

    @NotBlank
    private String productName;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal price;
}
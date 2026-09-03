package ru.yandex.practicum.commerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    @DecimalMin(value = "0.01")
    private BigDecimal price;

    private Long categoryId;
    private String imageUrl;
    private Boolean active;
}
package ru.yandex.practicum.commerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateProductRequest {
    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal price;

    private Long categoryId;
    private String imageUrl;
}
package ru.yandex.practicum.commerce.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInventoryRequest {
    @NotNull
    private Long productId;

    @NotNull
    @Min(0)
    private Integer quantity;
}
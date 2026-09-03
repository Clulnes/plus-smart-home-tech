package ru.yandex.practicum.commerce.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveResponse {
    private Boolean success;
    private Integer availableQuantity;
    private String message;
}
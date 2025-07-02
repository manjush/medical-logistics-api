package com.medical.logistics.interfaces.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    @NotBlank(message = "Item name is required")
    private String name;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}

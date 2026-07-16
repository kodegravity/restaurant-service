package com.fooddelivery.restaurant.dto.request;

import jakarta.validation.constraints.*;
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

    @NotBlank(message = "Menu item ID is required")
    private String menuItemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 20, message = "Quantity must not exceed 20")
    private Integer quantity;

    @NotNull(message = "Expected unit price is required")
    @DecimalMin(value = "0.01", message = "Expected unit price must be positive")
    private BigDecimal expectedUnitPrice;
}

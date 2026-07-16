package com.fooddelivery.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidatedOrderItemResponse {

    private UUID menuItemId;
    private String name;
    private Integer requestedQuantity;
    private Boolean available;
    private BigDecimal currentUnitPrice;
    private Boolean priceChanged;
    private BigDecimal lineTotal;
}

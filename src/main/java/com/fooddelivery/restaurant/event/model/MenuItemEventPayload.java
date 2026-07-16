package com.fooddelivery.restaurant.event.model;

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
public class MenuItemEventPayload {
    private UUID menuItemId;
    private UUID restaurantId;
    private UUID categoryId;
    private String name;
    private BigDecimal price;
    private String currency;
    private Boolean available;
}

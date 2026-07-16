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
public class RestaurantEventPayload {
    private UUID restaurantId;
    private UUID ownerId;
    private String name;
    private String status;
    private Boolean acceptingOrders;
    private BigDecimal minimumOrderAmount;
    private BigDecimal deliveryFee;
    private Integer estimatedPreparationMinutes;
    private String currency;
    private String city;
}

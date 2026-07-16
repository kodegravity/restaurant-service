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
public class DeliverySettingResponse {
    private UUID id;
    private BigDecimal deliveryRadiusKm;
    private BigDecimal minimumOrderAmount;
    private BigDecimal deliveryFee;
    private BigDecimal freeDeliveryThreshold;
    private Integer estimatedDeliveryMinutes;
    private Boolean deliveryEnabled;
    private Boolean pickupEnabled;
}

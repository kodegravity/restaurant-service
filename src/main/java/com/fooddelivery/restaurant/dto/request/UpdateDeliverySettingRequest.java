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
public class UpdateDeliverySettingRequest {

    @DecimalMin(value = "0.01", message = "Delivery radius must be positive")
    private BigDecimal deliveryRadiusKm;

    @DecimalMin(value = "0.0", message = "Minimum order amount must not be negative")
    private BigDecimal minimumOrderAmount;

    @DecimalMin(value = "0.0", message = "Delivery fee must not be negative")
    private BigDecimal deliveryFee;

    @DecimalMin(value = "0.0", message = "Free delivery threshold must not be negative")
    private BigDecimal freeDeliveryThreshold;

    @Min(value = 1, message = "Estimated delivery time must be at least 1 minute")
    private Integer estimatedDeliveryMinutes;

    @NotNull(message = "Delivery enabled status is required")
    private Boolean deliveryEnabled;

    @NotNull(message = "Pickup enabled status is required")
    private Boolean pickupEnabled;
}

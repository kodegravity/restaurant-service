package com.fooddelivery.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateOrderResponse {

    private UUID restaurantId;
    private Boolean valid;
    private Boolean restaurantOpen;
    private Boolean acceptingOrders;
    private String currency;
    private BigDecimal minimumOrderAmount;
    private BigDecimal deliveryFee;
    private Integer estimatedPreparationMinutes;
    private BigDecimal subtotal;
    
    @Builder.Default
    private List<String> validationErrors = new ArrayList<>();
    
    @Builder.Default
    private List<ValidatedOrderItemResponse> items = new ArrayList<>();
}

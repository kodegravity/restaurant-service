package com.fooddelivery.restaurant.dto.response;

import com.fooddelivery.restaurant.entity.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSummaryResponse {
    private UUID id;
    private String name;
    private String description;
    private RestaurantStatus status;
    private Boolean acceptingOrders;
    private BigDecimal deliveryFee;
    private Integer estimatedPreparationMinutes;
    private BigDecimal averageRating;
    private Integer totalRatings;
    private String currency;
    private String city;
    private List<String> cuisineNames;
}

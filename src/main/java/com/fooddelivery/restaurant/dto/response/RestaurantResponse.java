package com.fooddelivery.restaurant.dto.response;

import com.fooddelivery.restaurant.entity.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private UUID id;
    private UUID ownerId;
    private String name;
    private String description;
    private String phone;
    private String email;
    private RestaurantStatus status;
    private Boolean acceptingOrders;
    private BigDecimal minimumOrderAmount;
    private BigDecimal deliveryFee;
    private Integer estimatedPreparationMinutes;
    private BigDecimal averageRating;
    private Integer totalRatings;
    private String currency;
    private String timezone;
    private Boolean currentlyOpen;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private AddressResponse address;
    private List<CuisineResponse> cuisines;
    private DeliverySettingResponse deliverySetting;
}

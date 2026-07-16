package com.fooddelivery.restaurant.dto.request;

import com.fooddelivery.restaurant.entity.RestaurantStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRestaurantStatusRequest {

    @NotNull(message = "Status is required")
    private RestaurantStatus status;
}

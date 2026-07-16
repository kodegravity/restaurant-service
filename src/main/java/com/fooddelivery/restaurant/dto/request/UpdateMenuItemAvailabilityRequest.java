package com.fooddelivery.restaurant.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMenuItemAvailabilityRequest {

    @NotNull(message = "Available status is required")
    private Boolean available;
}

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
public class UpdateAcceptingOrdersRequest {

    @NotNull(message = "Accepting orders flag is required")
    private Boolean acceptingOrders;
}

package com.fooddelivery.restaurant.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateOrderRequest {

    @NotBlank(message = "Order type is required")
    private String orderType;

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<OrderItemRequest> items;
}

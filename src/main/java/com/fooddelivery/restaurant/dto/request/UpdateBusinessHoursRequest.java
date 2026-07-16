package com.fooddelivery.restaurant.dto.request;

import jakarta.validation.Valid;
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
public class UpdateBusinessHoursRequest {

    @NotEmpty(message = "Business hours list cannot be empty")
    @Valid
    private List<BusinessHourRequest> businessHours;
}

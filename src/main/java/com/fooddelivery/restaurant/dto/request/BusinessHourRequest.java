package com.fooddelivery.restaurant.dto.request;

import com.fooddelivery.restaurant.entity.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessHourRequest {

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    private LocalTime openTime;

    private LocalTime closeTime;

    @NotNull(message = "Closed flag is required")
    private Boolean closed;
}

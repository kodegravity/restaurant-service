package com.fooddelivery.restaurant.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryEventPayload {
    private UUID categoryId;
    private UUID restaurantId;
    private String name;
    private Boolean active;
}

package com.fooddelivery.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private UUID id;
    private UUID restaurantId;
    private UUID categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String imageUrl;
    private Boolean available;
    private Boolean vegetarian;
    private Boolean vegan;
    private Boolean glutenFree;
    private Integer spicyLevel;
    private Integer preparationMinutes;
    private Integer displayOrder;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

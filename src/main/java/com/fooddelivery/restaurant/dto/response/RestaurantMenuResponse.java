package com.fooddelivery.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantMenuResponse {
    private String restaurantName;
    private String currency;
    private List<MenuCategoryWithItemsResponse> categories;
}

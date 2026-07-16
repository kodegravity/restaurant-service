package com.fooddelivery.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryWithItemsResponse {
    private UUID id;
    private String name;
    private String description;
    private Integer displayOrder;
    private List<MenuItemResponse> items;
}

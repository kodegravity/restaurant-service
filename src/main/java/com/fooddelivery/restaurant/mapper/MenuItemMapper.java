package com.fooddelivery.restaurant.mapper;

import com.fooddelivery.restaurant.dto.request.CreateMenuItemRequest;
import com.fooddelivery.restaurant.dto.request.UpdateMenuItemRequest;
import com.fooddelivery.restaurant.dto.response.MenuItemResponse;
import com.fooddelivery.restaurant.entity.MenuItem;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurantId", source = "restaurantId")
    @Mapping(target = "categoryId", source = "request.categoryId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    MenuItem toEntity(CreateMenuItemRequest request, UUID restaurantId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurantId", ignore = true)
    @Mapping(target = "categoryId", source = "categoryId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(UpdateMenuItemRequest request, @MappingTarget MenuItem menuItem);

    MenuItemResponse toResponse(MenuItem menuItem);

    List<MenuItemResponse> toResponseList(List<MenuItem> menuItems);
}

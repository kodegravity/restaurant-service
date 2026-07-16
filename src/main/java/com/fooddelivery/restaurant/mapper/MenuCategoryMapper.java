package com.fooddelivery.restaurant.mapper;

import com.fooddelivery.restaurant.dto.request.CreateMenuCategoryRequest;
import com.fooddelivery.restaurant.dto.request.UpdateMenuCategoryRequest;
import com.fooddelivery.restaurant.dto.response.MenuCategoryResponse;
import com.fooddelivery.restaurant.entity.MenuCategory;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuCategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurantId", source = "restaurantId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    MenuCategory toEntity(CreateMenuCategoryRequest request, UUID restaurantId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(UpdateMenuCategoryRequest request, @MappingTarget MenuCategory category);

    MenuCategoryResponse toResponse(MenuCategory category);

    List<MenuCategoryResponse> toResponseList(List<MenuCategory> categories);
}

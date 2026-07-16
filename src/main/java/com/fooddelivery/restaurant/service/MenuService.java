package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.response.MenuCategoryWithItemsResponse;
import com.fooddelivery.restaurant.dto.response.MenuItemResponse;
import com.fooddelivery.restaurant.dto.response.RestaurantMenuResponse;
import com.fooddelivery.restaurant.entity.MenuCategory;
import com.fooddelivery.restaurant.entity.MenuItem;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.exception.RestaurantNotFoundException;
import com.fooddelivery.restaurant.mapper.MenuItemMapper;
import com.fooddelivery.restaurant.repository.MenuCategoryRepository;
import com.fooddelivery.restaurant.repository.MenuItemRepository;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemMapper menuItemMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "restaurant-menu", key = "#restaurantId")
    public RestaurantMenuResponse getRestaurantMenu(UUID restaurantId) {
        log.info("Fetching public menu for restaurant: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        List<MenuCategory> activeCategories = menuCategoryRepository
                .findByRestaurantIdAndActiveTrueOrderByDisplayOrderAsc(restaurantId);

        List<MenuItem> availableItems = menuItemRepository.findAvailableMenuItemsByRestaurantId(restaurantId);

        Map<UUID, List<MenuItem>> itemsByCategoryId = availableItems.stream()
                .collect(Collectors.groupingBy(MenuItem::getCategoryId));

        List<MenuCategoryWithItemsResponse> categoriesWithItems = activeCategories.stream()
                .map(category -> {
                    List<MenuItem> categoryItems = itemsByCategoryId.getOrDefault(category.getId(), Collections.emptyList());
                    List<MenuItemResponse> itemResponses = menuItemMapper.toResponseList(categoryItems);
                    
                    return MenuCategoryWithItemsResponse.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .description(category.getDescription())
                            .displayOrder(category.getDisplayOrder())
                            .items(itemResponses)
                            .build();
                })
                .filter(category -> !category.getItems().isEmpty())
                .collect(Collectors.toList());

        return RestaurantMenuResponse.builder()
                .restaurantName(restaurant.getName())
                .currency(restaurant.getCurrency())
                .categories(categoriesWithItems)
                .build();
    }
}

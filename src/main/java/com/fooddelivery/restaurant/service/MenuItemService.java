package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.request.CreateMenuItemRequest;
import com.fooddelivery.restaurant.dto.request.UpdateMenuItemAvailabilityRequest;
import com.fooddelivery.restaurant.dto.request.UpdateMenuItemRequest;
import com.fooddelivery.restaurant.dto.response.MenuItemResponse;
import com.fooddelivery.restaurant.entity.MenuCategory;
import com.fooddelivery.restaurant.entity.MenuItem;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.exception.*;
import com.fooddelivery.restaurant.mapper.MenuItemMapper;
import com.fooddelivery.restaurant.repository.MenuCategoryRepository;
import com.fooddelivery.restaurant.repository.MenuItemRepository;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemMapper menuItemMapper;
    private final OutboxService outboxService;

    @Transactional
    @CacheEvict(value = "restaurant-menu", key = "#restaurantId")
    public MenuItemResponse createMenuItem(UUID restaurantId, CreateMenuItemRequest request, UUID userId) {
        log.info("Creating menu item for restaurant: {} by user: {}", restaurantId, userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        validateOwnership(restaurant, userId);

        UUID categoryId = UUID.fromString(request.getCategoryId());
        MenuCategory category = menuCategoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> new MenuCategoryNotFoundException(categoryId));

        if (!request.getCurrency().equals(restaurant.getCurrency())) {
            throw new BusinessRuleViolationException("Menu item currency must match restaurant currency");
        }

        MenuItem menuItem = menuItemMapper.toEntity(request, restaurantId);
        menuItem.setCategoryId(categoryId);
        
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        outboxService.publishMenuItemCreated(savedMenuItem);

        log.info("Menu item created with id: {}", savedMenuItem.getId());
        return menuItemMapper.toResponse(savedMenuItem);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItems(UUID restaurantId, UUID userId) {
        log.info("Fetching menu items for restaurant: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        validateOwnership(restaurant, userId);

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdOrderByDisplayOrderAsc(restaurantId);
        return menuItemMapper.toResponseList(menuItems);
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItem(UUID restaurantId, UUID menuItemId) {
        log.info("Fetching menu item: {} for restaurant: {}", menuItemId, restaurantId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));

        return menuItemMapper.toResponse(menuItem);
    }

    @Transactional
    @CacheEvict(value = "restaurant-menu", key = "#restaurantId")
    public MenuItemResponse updateMenuItem(UUID restaurantId, UUID menuItemId, UpdateMenuItemRequest request, UUID userId) {
        log.info("Updating menu item: {} for restaurant: {} by user: {}", menuItemId, restaurantId, userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        validateOwnership(restaurant, userId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));

        UUID categoryId = UUID.fromString(request.getCategoryId());
        MenuCategory category = menuCategoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> new MenuCategoryNotFoundException(categoryId));

        if (!request.getCurrency().equals(restaurant.getCurrency())) {
            throw new BusinessRuleViolationException("Menu item currency must match restaurant currency");
        }

        menuItemMapper.updateEntityFromRequest(request, menuItem);
        menuItem.setCategoryId(categoryId);
        
        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);

        outboxService.publishMenuItemUpdated(updatedMenuItem);

        log.info("Menu item updated: {}", menuItemId);
        return menuItemMapper.toResponse(updatedMenuItem);
    }

    @Transactional
    @CacheEvict(value = "restaurant-menu", key = "#restaurantId")
    public MenuItemResponse updateMenuItemAvailability(UUID restaurantId, UUID menuItemId, UpdateMenuItemAvailabilityRequest request, UUID userId) {
        log.info("Updating menu item availability: {} for restaurant: {} to {} by user: {}", 
                menuItemId, restaurantId, request.getAvailable(), userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        validateOwnership(restaurant, userId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));

        menuItem.setAvailable(request.getAvailable());
        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);

        outboxService.publishMenuItemAvailabilityChanged(updatedMenuItem);

        log.info("Menu item availability updated: {}", menuItemId);
        return menuItemMapper.toResponse(updatedMenuItem);
    }

    @Transactional
    @CacheEvict(value = "restaurant-menu", key = "#restaurantId")
    public void deleteMenuItem(UUID restaurantId, UUID menuItemId, UUID userId) {
        log.info("Soft deleting menu item: {} for restaurant: {} by user: {}", menuItemId, restaurantId, userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        validateOwnership(restaurant, userId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));

        menuItem.setAvailable(false);
        menuItemRepository.save(menuItem);

        log.info("Menu item soft deleted: {}", menuItemId);
    }

    private void validateOwnership(Restaurant restaurant, UUID userId) {
        if (!restaurant.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to modify this restaurant");
        }
    }
}

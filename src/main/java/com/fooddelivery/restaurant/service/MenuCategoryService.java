package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.request.CreateMenuCategoryRequest;
import com.fooddelivery.restaurant.dto.request.UpdateCategoryAvailabilityRequest;
import com.fooddelivery.restaurant.dto.request.UpdateMenuCategoryRequest;
import com.fooddelivery.restaurant.dto.response.MenuCategoryResponse;
import com.fooddelivery.restaurant.entity.MenuCategory;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.exception.AccessDeniedException;
import com.fooddelivery.restaurant.exception.MenuCategoryNotFoundException;
import com.fooddelivery.restaurant.exception.RestaurantNotFoundException;
import com.fooddelivery.restaurant.mapper.MenuCategoryMapper;
import com.fooddelivery.restaurant.repository.MenuCategoryRepository;
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
public class MenuCategoryService {

    private final MenuCategoryRepository menuCategoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryMapper menuCategoryMapper;
    private final OutboxService outboxService;

    @Transactional
    @CacheEvict(value = "restaurant-menu", key = "#restaurantId")
    public MenuCategoryResponse createMenuCategory(UUID restaurantId, CreateMenuCategoryRequest request, UUID userId) {
        log.info("Creating menu category for restaurant: {} by user: {}", restaurantId, userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        validateOwnership(restaurant, userId);

        MenuCategory category = menuCategoryMapper.toEntity(request, restaurantId);
        MenuCategory savedCategory = menuCategoryRepository.save(category);

        outboxService.publishMenuCategoryCreated(savedCategory);

        log.info("Menu category created with id: {}", savedCategory.getId());
        return menuCategoryMapper.toResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<MenuCategoryResponse> getMenuCategories(UUID restaurantId, UUID userId) {
        log.info("Fetching menu categories for restaurant: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        validateOwnership(restaurant, userId);

        List<MenuCategory> categories = menuCategoryRepository.findByRestaurantIdOrderByDisplayOrderAsc(restaurantId);
        return menuCategoryMapper.toResponseList(categories);
    }

    @Transactional
    @CacheEvict(value = "restaurant-menu", key = "#restaurantId")
    public MenuCategoryResponse updateMenuCategory(UUID restaurantId, UUID categoryId, UpdateMenuCategoryRequest request, UUID userId) {
        log.info("Updating menu category: {} for restaurant: {} by user: {}", categoryId, restaurantId, userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        validateOwnership(restaurant, userId);

        MenuCategory category = menuCategoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> new MenuCategoryNotFoundException(categoryId));

        menuCategoryMapper.updateEntityFromRequest(request, category);
        MenuCategory updatedCategory = menuCategoryRepository.save(category);

        outboxService.publishMenuCategoryUpdated(updatedCategory);

        log.info("Menu category updated: {}", categoryId);
        return menuCategoryMapper.toResponse(updatedCategory);
    }

    @Transactional
    @CacheEvict(value = "restaurant-menu", key = "#restaurantId")
    public MenuCategoryResponse updateCategoryAvailability(UUID restaurantId, UUID categoryId, UpdateCategoryAvailabilityRequest request, UUID userId) {
        log.info("Updating category availability: {} for restaurant: {} to {} by user: {}", 
                categoryId, restaurantId, request.getActive(), userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        validateOwnership(restaurant, userId);

        MenuCategory category = menuCategoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> new MenuCategoryNotFoundException(categoryId));

        category.setActive(request.getActive());
        MenuCategory updatedCategory = menuCategoryRepository.save(category);

        if (!request.getActive()) {
            outboxService.publishMenuCategoryDeactivated(updatedCategory);
        }

        log.info("Category availability updated: {}", categoryId);
        return menuCategoryMapper.toResponse(updatedCategory);
    }

    @Transactional
    @CacheEvict(value = "restaurant-menu", key = "#restaurantId")
    public void deleteMenuCategory(UUID restaurantId, UUID categoryId, UUID userId) {
        log.info("Soft deleting menu category: {} for restaurant: {} by user: {}", categoryId, restaurantId, userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        validateOwnership(restaurant, userId);

        MenuCategory category = menuCategoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> new MenuCategoryNotFoundException(categoryId));

        category.setActive(false);
        menuCategoryRepository.save(category);

        log.info("Menu category soft deleted: {}", categoryId);
    }

    private void validateOwnership(Restaurant restaurant, UUID userId) {
        if (!restaurant.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to modify this restaurant");
        }
    }
}

package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.request.OrderItemRequest;
import com.fooddelivery.restaurant.dto.request.ValidateOrderRequest;
import com.fooddelivery.restaurant.dto.response.ValidateOrderResponse;
import com.fooddelivery.restaurant.dto.response.ValidatedOrderItemResponse;
import com.fooddelivery.restaurant.entity.*;
import com.fooddelivery.restaurant.exception.RestaurantNotFoundException;
import com.fooddelivery.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderValidationService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final BusinessHourRepository businessHourRepository;
    private final RestaurantAvailabilityService availabilityService;

    @Transactional(readOnly = true)
    public ValidateOrderResponse validateOrder(UUID restaurantId, ValidateOrderRequest request) {
        log.info("Validating order for restaurant: {}", restaurantId);

        ValidateOrderResponse.ValidateOrderResponseBuilder responseBuilder = ValidateOrderResponse.builder()
                .restaurantId(restaurantId)
                .valid(true)
                .validationErrors(new ArrayList<>())
                .items(new ArrayList<>());

        // Load restaurant
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        responseBuilder.currency(restaurant.getCurrency());
        responseBuilder.estimatedPreparationMinutes(restaurant.getEstimatedPreparationMinutes());

        // Check restaurant status
        if (restaurant.getStatus() != RestaurantStatus.ACTIVE) {
            responseBuilder.valid(false);
            responseBuilder.validationErrors(List.of("RESTAURANT_NOT_ACTIVE"));
            responseBuilder.restaurantOpen(false);
            responseBuilder.acceptingOrders(false);
            return responseBuilder.build();
        }

        // Check if restaurant is accepting orders
        responseBuilder.acceptingOrders(restaurant.getAcceptingOrders());
        if (!restaurant.getAcceptingOrders()) {
            responseBuilder.valid(false);
            responseBuilder.validationErrors(List.of("RESTAURANT_NOT_ACCEPTING_ORDERS"));
        }

        // Check if restaurant is open
        List<BusinessHour> businessHours = businessHourRepository.findByRestaurantIdOrderByDayOfWeek(restaurantId);
        boolean isOpen = availabilityService.isRestaurantOpen(restaurant, businessHours, ZonedDateTime.now());
        responseBuilder.restaurantOpen(isOpen);
        
        if (!isOpen) {
            responseBuilder.valid(false);
            List<String> errors = new ArrayList<>(responseBuilder.build().getValidationErrors());
            errors.add("RESTAURANT_CLOSED");
            responseBuilder.validationErrors(errors);
        }

        // Check order type support
        DeliverySetting deliverySetting = restaurant.getDeliverySetting();
        if (deliverySetting != null) {
            responseBuilder.minimumOrderAmount(deliverySetting.getMinimumOrderAmount());
            responseBuilder.deliveryFee(deliverySetting.getDeliveryFee());

            if ("DELIVERY".equalsIgnoreCase(request.getOrderType()) && !deliverySetting.getDeliveryEnabled()) {
                responseBuilder.valid(false);
                List<String> errors = new ArrayList<>(responseBuilder.build().getValidationErrors());
                errors.add("DELIVERY_NOT_ENABLED");
                responseBuilder.validationErrors(errors);
            }

            if ("PICKUP".equalsIgnoreCase(request.getOrderType()) && !deliverySetting.getPickupEnabled()) {
                responseBuilder.valid(false);
                List<String> errors = new ArrayList<>(responseBuilder.build().getValidationErrors());
                errors.add("PICKUP_NOT_ENABLED");
                responseBuilder.validationErrors(errors);
            }
        }

        // Load all requested menu items in one query
        Set<UUID> menuItemIds = request.getItems().stream()
                .map(item -> UUID.fromString(item.getMenuItemId()))
                .collect(Collectors.toSet());

        List<MenuItem> menuItems = menuItemRepository.findByIdIn(menuItemIds);
        Map<UUID, MenuItem> menuItemMap = menuItems.stream()
                .collect(Collectors.toMap(MenuItem::getId, item -> item));

        // Load categories for the menu items
        Set<UUID> categoryIds = menuItems.stream()
                .map(MenuItem::getCategoryId)
                .collect(Collectors.toSet());
        
        Map<UUID, MenuCategory> categoryMap = menuCategoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(MenuCategory::getId, cat -> cat));

        // Validate each item
        BigDecimal subtotal = BigDecimal.ZERO;
        List<String> itemErrors = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            UUID menuItemId = UUID.fromString(itemRequest.getMenuItemId());
            MenuItem menuItem = menuItemMap.get(menuItemId);

            ValidatedOrderItemResponse.ValidatedOrderItemResponseBuilder itemResponseBuilder = ValidatedOrderItemResponse.builder()
                    .menuItemId(menuItemId)
                    .requestedQuantity(itemRequest.getQuantity());

            if (menuItem == null) {
                itemResponseBuilder.available(false)
                        .currentUnitPrice(BigDecimal.ZERO)
                        .priceChanged(false)
                        .lineTotal(BigDecimal.ZERO)
                        .name("Unknown");
                itemErrors.add("MENU_ITEM_NOT_FOUND");
                responseBuilder.items(new ArrayList<>(responseBuilder.build().getItems()));
                responseBuilder.build().getItems().add(itemResponseBuilder.build());
                continue;
            }

            // Validate restaurant ownership
            if (!menuItem.getRestaurantId().equals(restaurantId)) {
                itemErrors.add("MENU_ITEM_NOT_FOUND");
                continue;
            }

            itemResponseBuilder.name(menuItem.getName());
            itemResponseBuilder.currentUnitPrice(menuItem.getPrice());

            // Check if category is active
            MenuCategory category = categoryMap.get(menuItem.getCategoryId());
            if (category == null || !category.getActive()) {
                itemResponseBuilder.available(false);
                itemErrors.add("MENU_CATEGORY_INACTIVE");
            } else {
                itemResponseBuilder.available(menuItem.getAvailable());
                if (!menuItem.getAvailable()) {
                    itemErrors.add("MENU_ITEM_UNAVAILABLE");
                }
            }

            // Check price
            boolean priceChanged = menuItem.getPrice().compareTo(itemRequest.getExpectedUnitPrice()) != 0;
            itemResponseBuilder.priceChanged(priceChanged);
            if (priceChanged) {
                itemErrors.add("PRICE_CHANGED");
            }

            // Calculate line total using current price
            BigDecimal lineTotal = menuItem.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            
            itemResponseBuilder.lineTotal(lineTotal);
            subtotal = subtotal.add(lineTotal);

            responseBuilder.build().getItems().add(itemResponseBuilder.build());
        }

        // Set subtotal
        responseBuilder.subtotal(subtotal.setScale(2, RoundingMode.HALF_UP));

        // Check minimum order amount
        if (deliverySetting != null && deliverySetting.getMinimumOrderAmount() != null) {
            if (subtotal.compareTo(deliverySetting.getMinimumOrderAmount()) < 0) {
                itemErrors.add("MINIMUM_ORDER_NOT_MET");
            }
        }

        // Add item errors to validation errors
        if (!itemErrors.isEmpty()) {
            responseBuilder.valid(false);
            List<String> allErrors = new ArrayList<>(responseBuilder.build().getValidationErrors());
            allErrors.addAll(itemErrors);
            responseBuilder.validationErrors(allErrors);
        }

        ValidateOrderResponse response = responseBuilder.build();
        log.info("Order validation completed for restaurant: {}. Valid: {}", restaurantId, response.getValid());
        
        return response;
    }
}

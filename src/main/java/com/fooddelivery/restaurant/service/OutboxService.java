package com.fooddelivery.restaurant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.restaurant.entity.*;
import com.fooddelivery.restaurant.event.model.*;
import com.fooddelivery.restaurant.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void publishRestaurantCreated(Restaurant restaurant) {
        log.info("Publishing RESTAURANT_CREATED event for restaurant: {}", restaurant.getId());
        RestaurantEventPayload payload = buildRestaurantPayload(restaurant);
        createOutboxEvent("RESTAURANT_CREATED", "RESTAURANT", restaurant.getId(), payload);
    }

    @Transactional
    public void publishRestaurantUpdated(Restaurant restaurant) {
        log.info("Publishing RESTAURANT_UPDATED event for restaurant: {}", restaurant.getId());
        RestaurantEventPayload payload = buildRestaurantPayload(restaurant);
        createOutboxEvent("RESTAURANT_UPDATED", "RESTAURANT", restaurant.getId(), payload);
    }

    @Transactional
    public void publishRestaurantActivated(Restaurant restaurant) {
        log.info("Publishing RESTAURANT_ACTIVATED event for restaurant: {}", restaurant.getId());
        RestaurantEventPayload payload = buildRestaurantPayload(restaurant);
        createOutboxEvent("RESTAURANT_ACTIVATED", "RESTAURANT", restaurant.getId(), payload);
    }

    @Transactional
    public void publishRestaurantDeactivated(Restaurant restaurant) {
        log.info("Publishing RESTAURANT_DEACTIVATED event for restaurant: {}", restaurant.getId());
        RestaurantEventPayload payload = buildRestaurantPayload(restaurant);
        createOutboxEvent("RESTAURANT_DEACTIVATED", "RESTAURANT", restaurant.getId(), payload);
    }

    @Transactional
    public void publishAcceptingOrdersChanged(Restaurant restaurant) {
        log.info("Publishing RESTAURANT_ACCEPTING_ORDERS_CHANGED event for restaurant: {}", restaurant.getId());
        RestaurantEventPayload payload = buildRestaurantPayload(restaurant);
        createOutboxEvent("RESTAURANT_ACCEPTING_ORDERS_CHANGED", "RESTAURANT", restaurant.getId(), payload);
    }

    @Transactional
    public void publishMenuCategoryCreated(MenuCategory category) {
        log.info("Publishing MENU_CATEGORY_CREATED event for category: {}", category.getId());
        MenuCategoryEventPayload payload = MenuCategoryEventPayload.builder()
                .categoryId(category.getId())
                .restaurantId(category.getRestaurantId())
                .name(category.getName())
                .active(category.getActive())
                .build();
        createOutboxEvent("MENU_CATEGORY_CREATED", "RESTAURANT", category.getRestaurantId(), payload);
    }

    @Transactional
    public void publishMenuCategoryUpdated(MenuCategory category) {
        log.info("Publishing MENU_CATEGORY_UPDATED event for category: {}", category.getId());
        MenuCategoryEventPayload payload = MenuCategoryEventPayload.builder()
                .categoryId(category.getId())
                .restaurantId(category.getRestaurantId())
                .name(category.getName())
                .active(category.getActive())
                .build();
        createOutboxEvent("MENU_CATEGORY_UPDATED", "RESTAURANT", category.getRestaurantId(), payload);
    }

    @Transactional
    public void publishMenuCategoryDeactivated(MenuCategory category) {
        log.info("Publishing MENU_CATEGORY_DEACTIVATED event for category: {}", category.getId());
        MenuCategoryEventPayload payload = MenuCategoryEventPayload.builder()
                .categoryId(category.getId())
                .restaurantId(category.getRestaurantId())
                .name(category.getName())
                .active(category.getActive())
                .build();
        createOutboxEvent("MENU_CATEGORY_DEACTIVATED", "RESTAURANT", category.getRestaurantId(), payload);
    }

    @Transactional
    public void publishMenuItemCreated(MenuItem menuItem) {
        log.info("Publishing MENU_ITEM_CREATED event for menu item: {}", menuItem.getId());
        MenuItemEventPayload payload = buildMenuItemPayload(menuItem);
        createOutboxEvent("MENU_ITEM_CREATED", "RESTAURANT", menuItem.getRestaurantId(), payload);
    }

    @Transactional
    public void publishMenuItemUpdated(MenuItem menuItem) {
        log.info("Publishing MENU_ITEM_UPDATED event for menu item: {}", menuItem.getId());
        MenuItemEventPayload payload = buildMenuItemPayload(menuItem);
        createOutboxEvent("MENU_ITEM_UPDATED", "RESTAURANT", menuItem.getRestaurantId(), payload);
    }

    @Transactional
    public void publishMenuItemAvailabilityChanged(MenuItem menuItem) {
        log.info("Publishing MENU_ITEM_AVAILABILITY_CHANGED event for menu item: {}", menuItem.getId());
        MenuItemEventPayload payload = buildMenuItemPayload(menuItem);
        createOutboxEvent("MENU_ITEM_AVAILABILITY_CHANGED", "RESTAURANT", menuItem.getRestaurantId(), payload);
    }

    @Transactional
    public void publishDeliverySettingsUpdated(Restaurant restaurant) {
        log.info("Publishing DELIVERY_SETTINGS_UPDATED event for restaurant: {}", restaurant.getId());
        RestaurantEventPayload payload = buildRestaurantPayload(restaurant);
        createOutboxEvent("DELIVERY_SETTINGS_UPDATED", "RESTAURANT", restaurant.getId(), payload);
    }

    private void createOutboxEvent(String eventType, String aggregateType, UUID aggregateId, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payloadJson)
                    .status(OutboxEventStatus.PENDING)
                    .retryCount(0)
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.debug("Outbox event created: {} for aggregate {}", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event payload", e);
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }

    private RestaurantEventPayload buildRestaurantPayload(Restaurant restaurant) {
        return RestaurantEventPayload.builder()
                .restaurantId(restaurant.getId())
                .ownerId(restaurant.getOwnerId())
                .name(restaurant.getName())
                .status(restaurant.getStatus().name())
                .acceptingOrders(restaurant.getAcceptingOrders())
                .minimumOrderAmount(restaurant.getMinimumOrderAmount())
                .deliveryFee(restaurant.getDeliveryFee())
                .estimatedPreparationMinutes(restaurant.getEstimatedPreparationMinutes())
                .currency(restaurant.getCurrency())
                .city(restaurant.getAddress() != null ? restaurant.getAddress().getCity() : null)
                .build();
    }

    private MenuItemEventPayload buildMenuItemPayload(MenuItem menuItem) {
        return MenuItemEventPayload.builder()
                .menuItemId(menuItem.getId())
                .restaurantId(menuItem.getRestaurantId())
                .categoryId(menuItem.getCategoryId())
                .name(menuItem.getName())
                .price(menuItem.getPrice())
                .currency(menuItem.getCurrency())
                .available(menuItem.getAvailable())
                .build();
    }
}

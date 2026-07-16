package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Slf4j
public class RestaurantAvailabilityService {

    public boolean isRestaurantOpen(Restaurant restaurant, List<BusinessHour> hours, ZonedDateTime currentTime) {
        if (restaurant == null || hours == null || hours.isEmpty()) {
            return false;
        }

        ZoneId restaurantZone = ZoneId.of(restaurant.getTimezone());
        ZonedDateTime restaurantTime = currentTime.withZoneSameInstant(restaurantZone);
        
        com.fooddelivery.restaurant.entity.DayOfWeek currentDay = mapJavaDayToCustomDay(
                restaurantTime.getDayOfWeek()
        );
        
        BusinessHour todayHours = hours.stream()
                .filter(h -> h.getDayOfWeek() == currentDay)
                .findFirst()
                .orElse(null);

        if (todayHours == null || todayHours.getClosed()) {
            return false;
        }

        LocalTime currentLocalTime = restaurantTime.toLocalTime();
        LocalTime openTime = todayHours.getOpenTime();
        LocalTime closeTime = todayHours.getCloseTime();

        if (openTime == null || closeTime == null) {
            return false;
        }

        // Handle overnight hours (e.g., 22:00 to 02:00)
        if (closeTime.isBefore(openTime)) {
            return currentLocalTime.isAfter(openTime) || currentLocalTime.isBefore(closeTime);
        }

        return !currentLocalTime.isBefore(openTime) && currentLocalTime.isBefore(closeTime);
    }

    public boolean isRestaurantEligibleForOrders(
            Restaurant restaurant,
            List<BusinessHour> hours,
            ZonedDateTime currentTime,
            String orderType) {
        
        if (restaurant.getStatus() != RestaurantStatus.ACTIVE) {
            log.debug("Restaurant {} is not ACTIVE, status: {}", restaurant.getId(), restaurant.getStatus());
            return false;
        }

        if (!restaurant.getAcceptingOrders()) {
            log.debug("Restaurant {} is not accepting orders", restaurant.getId());
            return false;
        }

        if (!isRestaurantOpen(restaurant, hours, currentTime)) {
            log.debug("Restaurant {} is currently closed", restaurant.getId());
            return false;
        }

        DeliverySetting deliverySetting = restaurant.getDeliverySetting();
        if (deliverySetting == null) {
            log.debug("Restaurant {} has no delivery settings", restaurant.getId());
            return false;
        }

        if ("DELIVERY".equalsIgnoreCase(orderType) && !deliverySetting.getDeliveryEnabled()) {
            log.debug("Restaurant {} does not support delivery", restaurant.getId());
            return false;
        }

        if ("PICKUP".equalsIgnoreCase(orderType) && !deliverySetting.getPickupEnabled()) {
            log.debug("Restaurant {} does not support pickup", restaurant.getId());
            return false;
        }

        return true;
    }

    private com.fooddelivery.restaurant.entity.DayOfWeek mapJavaDayToCustomDay(java.time.DayOfWeek javaDay) {
        return switch (javaDay) {
            case MONDAY -> com.fooddelivery.restaurant.entity.DayOfWeek.MONDAY;
            case TUESDAY -> com.fooddelivery.restaurant.entity.DayOfWeek.TUESDAY;
            case WEDNESDAY -> com.fooddelivery.restaurant.entity.DayOfWeek.WEDNESDAY;
            case THURSDAY -> com.fooddelivery.restaurant.entity.DayOfWeek.THURSDAY;
            case FRIDAY -> com.fooddelivery.restaurant.entity.DayOfWeek.FRIDAY;
            case SATURDAY -> com.fooddelivery.restaurant.entity.DayOfWeek.SATURDAY;
            case SUNDAY -> com.fooddelivery.restaurant.entity.DayOfWeek.SUNDAY;
        };
    }
}

package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.request.BusinessHourRequest;
import com.fooddelivery.restaurant.dto.request.UpdateBusinessHoursRequest;
import com.fooddelivery.restaurant.dto.response.BusinessHourResponse;
import com.fooddelivery.restaurant.entity.BusinessHour;
import com.fooddelivery.restaurant.entity.DayOfWeek;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.exception.AccessDeniedException;
import com.fooddelivery.restaurant.exception.RestaurantNotFoundException;
import com.fooddelivery.restaurant.mapper.BusinessHourMapper;
import com.fooddelivery.restaurant.repository.BusinessHourRepository;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessHourService {

    private final BusinessHourRepository businessHourRepository;
    private final RestaurantRepository restaurantRepository;
    private final BusinessHourMapper businessHourMapper;

    @Transactional(readOnly = true)
    public List<BusinessHourResponse> getBusinessHours(UUID restaurantId) {
        log.info("Fetching business hours for restaurant: {}", restaurantId);
        
        List<BusinessHour> businessHours = businessHourRepository.findByRestaurantIdOrderByDayOfWeek(restaurantId);
        return businessHourMapper.toResponseList(businessHours);
    }

    @Transactional
    @CacheEvict(value = "restaurant-details", key = "#restaurantId")
    public List<BusinessHourResponse> updateBusinessHours(UUID restaurantId, UpdateBusinessHoursRequest request, UUID userId) {
        log.info("Updating business hours for restaurant: {} by user: {}", restaurantId, userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        if (!restaurant.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to modify this restaurant");
        }

        // Delete existing business hours
        businessHourRepository.deleteByRestaurantId(restaurantId);

        // Create new business hours
        List<BusinessHour> newBusinessHours = new ArrayList<>();
        for (BusinessHourRequest hourRequest : request.getBusinessHours()) {
            BusinessHour businessHour = businessHourMapper.toEntity(hourRequest, restaurantId);
            newBusinessHours.add(businessHour);
        }

        List<BusinessHour> savedHours = businessHourRepository.saveAll(newBusinessHours);

        log.info("Business hours updated for restaurant: {}", restaurantId);
        return businessHourMapper.toResponseList(savedHours);
    }
}

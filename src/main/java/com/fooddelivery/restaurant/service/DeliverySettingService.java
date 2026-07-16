package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.request.UpdateDeliverySettingRequest;
import com.fooddelivery.restaurant.dto.response.DeliverySettingResponse;
import com.fooddelivery.restaurant.entity.DeliverySetting;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.exception.AccessDeniedException;
import com.fooddelivery.restaurant.exception.BusinessRuleViolationException;
import com.fooddelivery.restaurant.exception.RestaurantNotFoundException;
import com.fooddelivery.restaurant.mapper.DeliverySettingMapper;
import com.fooddelivery.restaurant.repository.DeliverySettingRepository;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliverySettingService {

    private final DeliverySettingRepository deliverySettingRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliverySettingMapper deliverySettingMapper;
    private final OutboxService outboxService;

    @Transactional(readOnly = true)
    public DeliverySettingResponse getDeliverySettings(UUID restaurantId) {
        log.info("Fetching delivery settings for restaurant: {}", restaurantId);

        DeliverySetting deliverySetting = deliverySettingRepository.findByRestaurantId(restaurantId)
                .orElse(null);

        return deliverySetting != null ? deliverySettingMapper.toResponse(deliverySetting) : null;
    }

    @Transactional
    @CacheEvict(value = "restaurant-details", key = "#restaurantId")
    public DeliverySettingResponse updateDeliverySettings(UUID restaurantId, UpdateDeliverySettingRequest request, UUID userId) {
        log.info("Updating delivery settings for restaurant: {} by user: {}", restaurantId, userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        if (!restaurant.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to modify this restaurant");
        }

        validateDeliverySettings(request);

        DeliverySetting deliverySetting = deliverySettingRepository.findByRestaurantId(restaurantId)
                .orElseGet(() -> {
                    DeliverySetting newSetting = new DeliverySetting();
                    newSetting.setRestaurant(restaurant);
                    return newSetting;
                });

        deliverySettingMapper.updateEntityFromRequest(request, deliverySetting);
        DeliverySetting updatedSetting = deliverySettingRepository.save(deliverySetting);

        outboxService.publishDeliverySettingsUpdated(restaurant);

        log.info("Delivery settings updated for restaurant: {}", restaurantId);
        return deliverySettingMapper.toResponse(updatedSetting);
    }

    private void validateDeliverySettings(UpdateDeliverySettingRequest request) {
        if (!request.getDeliveryEnabled() && !request.getPickupEnabled()) {
            throw new BusinessRuleViolationException("At least one of delivery or pickup must be enabled");
        }

        if (request.getDeliveryEnabled() && request.getDeliveryRadiusKm() != null 
                && request.getDeliveryRadiusKm().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException("Delivery radius must be positive when delivery is enabled");
        }

        if (request.getFreeDeliveryThreshold() != null && request.getMinimumOrderAmount() != null) {
            if (request.getFreeDeliveryThreshold().compareTo(request.getMinimumOrderAmount()) < 0) {
                throw new BusinessRuleViolationException("Free delivery threshold must be greater than or equal to minimum order amount");
            }
        }
    }
}

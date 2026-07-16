package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.request.*;
import com.fooddelivery.restaurant.dto.response.*;
import com.fooddelivery.restaurant.entity.*;
import com.fooddelivery.restaurant.exception.*;
import com.fooddelivery.restaurant.mapper.*;
import com.fooddelivery.restaurant.repository.*;
import com.fooddelivery.restaurant.specification.RestaurantSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantAddressRepository addressRepository;
    private final CuisineRepository cuisineRepository;
    private final BusinessHourRepository businessHourRepository;
    private final DeliverySettingRepository deliverySettingRepository;
    private final RestaurantMapper restaurantMapper;
    private final CuisineMapper cuisineMapper;
    private final BusinessHourMapper businessHourMapper;
    private final DeliverySettingMapper deliverySettingMapper;
    private final RestaurantAvailabilityService availabilityService;
    private final OutboxService outboxService;

    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantRequest request, UUID ownerId) {
        log.info("Creating restaurant for owner: {}", ownerId);

        Restaurant restaurant = restaurantMapper.toEntity(request, ownerId);
        
        if (request.getAddress() != null) {
            RestaurantAddress address = restaurantMapper.toAddressEntity(request.getAddress());
            restaurant.setAddress(address);
        }

        if (request.getCuisineIds() != null && !request.getCuisineIds().isEmpty()) {
            Set<UUID> cuisineIds = request.getCuisineIds().stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
            List<Cuisine> cuisines = cuisineRepository.findByIdIn(cuisineIds);
            restaurant.setCuisines(new HashSet<>(cuisines));
        }

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        
        outboxService.publishRestaurantCreated(savedRestaurant);
        
        log.info("Restaurant created with id: {}", savedRestaurant.getId());
        return restaurantMapper.toResponse(savedRestaurant, false);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantSummaryResponse> searchRestaurants(
            String city,
            String postalCode,
            String cuisine,
            String name,
            Boolean acceptingOrders,
            Pageable pageable) {
        
        log.info("Searching restaurants with filters: city={}, postalCode={}, cuisine={}, name={}, acceptingOrders={}",
                city, postalCode, cuisine, name, acceptingOrders);

        Specification<Restaurant> spec = Specification.where(RestaurantSpecification.isActive());

        if (city != null && !city.isBlank()) {
            spec = spec.and(RestaurantSpecification.hasCity(city));
        }

        if (postalCode != null && !postalCode.isBlank()) {
            spec = spec.and(RestaurantSpecification.hasPostalCode(postalCode));
        }

        if (cuisine != null && !cuisine.isBlank()) {
            UUID cuisineId = UUID.fromString(cuisine);
            spec = spec.and(RestaurantSpecification.hasCuisine(cuisineId));
        }

        if (name != null && !name.isBlank()) {
            spec = spec.and(RestaurantSpecification.hasName(name));
        }

        if (acceptingOrders != null) {
            spec = spec.and(RestaurantSpecification.isAcceptingOrders(acceptingOrders));
        }

        return restaurantRepository.findAll(spec, pageable)
                .map(restaurantMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "restaurant-details", key = "#restaurantId")
    public RestaurantResponse getRestaurant(UUID restaurantId) {
        log.info("Fetching restaurant: {}", restaurantId);
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        List<BusinessHour> businessHours = businessHourRepository.findByRestaurantIdOrderByDayOfWeek(restaurantId);
        boolean isOpen = availabilityService.isRestaurantOpen(restaurant, businessHours, ZonedDateTime.now());

        return restaurantMapper.toResponse(restaurant, isOpen);
    }

    @Transactional
    @CacheEvict(value = "restaurant-details", key = "#restaurantId")
    public RestaurantResponse updateRestaurant(UUID restaurantId, UpdateRestaurantRequest request, UUID userId) {
        log.info("Updating restaurant: {} by user: {}", restaurantId, userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        validateOwnership(restaurant, userId);

        restaurantMapper.updateEntityFromRequest(request, restaurant);

        if (request.getAddress() != null) {
            if (restaurant.getAddress() != null) {
                restaurantMapper.updateAddressFromRequest(request.getAddress(), restaurant.getAddress());
            } else {
                RestaurantAddress newAddress = restaurantMapper.toAddressEntity(request.getAddress());
                restaurant.setAddress(newAddress);
            }
        }

        if (request.getCuisineIds() != null) {
            Set<UUID> cuisineIds = request.getCuisineIds().stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
            List<Cuisine> cuisines = cuisineRepository.findByIdIn(cuisineIds);
            restaurant.setCuisines(new HashSet<>(cuisines));
        }

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        
        outboxService.publishRestaurantUpdated(updatedRestaurant);
        
        log.info("Restaurant updated: {}", restaurantId);
        return restaurantMapper.toResponse(updatedRestaurant, false);
    }

    @Transactional
    @CacheEvict(value = "restaurant-details", key = "#restaurantId")
    public RestaurantResponse updateRestaurantStatus(UUID restaurantId, UpdateRestaurantStatusRequest request, UUID userId, boolean isAdmin) {
        log.info("Updating restaurant status: {} to {} by user: {}", restaurantId, request.getStatus(), userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        RestaurantStatus currentStatus = restaurant.getStatus();
        RestaurantStatus newStatus = request.getStatus();

        if (!isAdmin) {
            validateOwnership(restaurant, userId);
            
            if (currentStatus == RestaurantStatus.DRAFT && newStatus == RestaurantStatus.PENDING_APPROVAL) {
                // Owner can submit for approval
            } else {
                throw new AccessDeniedException("Only administrators can change restaurant status to " + newStatus);
            }
        }

        restaurant.setStatus(newStatus);
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);

        if (newStatus == RestaurantStatus.ACTIVE) {
            outboxService.publishRestaurantActivated(updatedRestaurant);
        } else if (newStatus == RestaurantStatus.INACTIVE || newStatus == RestaurantStatus.SUSPENDED) {
            outboxService.publishRestaurantDeactivated(updatedRestaurant);
        }

        log.info("Restaurant status updated: {} from {} to {}", restaurantId, currentStatus, newStatus);
        return restaurantMapper.toResponse(updatedRestaurant, false);
    }

    @Transactional
    @CacheEvict(value = "restaurant-details", key = "#restaurantId")
    public RestaurantResponse updateAcceptingOrders(UUID restaurantId, UpdateAcceptingOrdersRequest request, UUID userId) {
        log.info("Updating accepting orders: {} to {} by user: {}", restaurantId, request.getAcceptingOrders(), userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        validateOwnership(restaurant, userId);

        if (request.getAcceptingOrders() && restaurant.getStatus() != RestaurantStatus.ACTIVE) {
            throw new BusinessRuleViolationException("Restaurant must be ACTIVE to accept orders");
        }

        restaurant.setAcceptingOrders(request.getAcceptingOrders());
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);

        outboxService.publishAcceptingOrdersChanged(updatedRestaurant);

        log.info("Restaurant accepting orders updated: {}", restaurantId);
        return restaurantMapper.toResponse(updatedRestaurant, false);
    }

    @Transactional
    @CacheEvict(value = "restaurant-details", key = "#restaurantId")
    public void deleteRestaurant(UUID restaurantId, UUID userId) {
        log.info("Soft deleting restaurant: {} by user: {}", restaurantId, userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        validateOwnership(restaurant, userId);

        restaurant.setStatus(RestaurantStatus.INACTIVE);
        restaurantRepository.save(restaurant);

        log.info("Restaurant soft deleted: {}", restaurantId);
    }

    private void validateOwnership(Restaurant restaurant, UUID userId) {
        if (!restaurant.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to modify this restaurant");
        }
    }
}

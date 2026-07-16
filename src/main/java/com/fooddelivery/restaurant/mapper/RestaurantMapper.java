package com.fooddelivery.restaurant.mapper;

import com.fooddelivery.restaurant.dto.request.AddressRequest;
import com.fooddelivery.restaurant.dto.request.CreateRestaurantRequest;
import com.fooddelivery.restaurant.dto.request.UpdateRestaurantRequest;
import com.fooddelivery.restaurant.dto.response.AddressResponse;
import com.fooddelivery.restaurant.dto.response.RestaurantResponse;
import com.fooddelivery.restaurant.dto.response.RestaurantSummaryResponse;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.entity.RestaurantAddress;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RestaurantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", source = "ownerId")
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "acceptingOrders", constant = "false")
    @Mapping(target = "totalRatings", constant = "0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "cuisines", ignore = true)
    @Mapping(target = "deliverySetting", ignore = true)
    Restaurant toEntity(CreateRestaurantRequest request, UUID ownerId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "acceptingOrders", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalRatings", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "timezone", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "cuisines", ignore = true)
    @Mapping(target = "deliverySetting", ignore = true)
    void updateEntityFromRequest(UpdateRestaurantRequest request, @MappingTarget Restaurant restaurant);

    @Mapping(target = "currentlyOpen", source = "currentlyOpen")
    @Mapping(target = "cuisines", source = "restaurant.cuisines")
    RestaurantResponse toResponse(Restaurant restaurant, Boolean currentlyOpen);

    RestaurantSummaryResponse toSummaryResponse(Restaurant restaurant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RestaurantAddress toAddressEntity(AddressRequest request);

    AddressResponse toAddressResponse(RestaurantAddress address);

    void updateAddressFromRequest(AddressRequest request, @MappingTarget RestaurantAddress address);
}

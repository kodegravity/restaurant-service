package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.entity.RestaurantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID>, JpaSpecificationExecutor<Restaurant> {

    Optional<Restaurant> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<Restaurant> findByOwnerId(UUID ownerId);

    List<Restaurant> findByStatus(RestaurantStatus status);

    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);
}

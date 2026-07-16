package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.entity.RestaurantAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantAddressRepository extends JpaRepository<RestaurantAddress, UUID> {

    Optional<RestaurantAddress> findByRestaurantId(UUID restaurantId);

    void deleteByRestaurantId(UUID restaurantId);
}

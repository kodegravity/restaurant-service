package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.entity.DeliverySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliverySettingRepository extends JpaRepository<DeliverySetting, UUID> {

    Optional<DeliverySetting> findByRestaurantId(UUID restaurantId);

    void deleteByRestaurantId(UUID restaurantId);
}

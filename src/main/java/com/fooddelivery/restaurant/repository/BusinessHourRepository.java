package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.entity.BusinessHour;
import com.fooddelivery.restaurant.entity.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessHourRepository extends JpaRepository<BusinessHour, UUID> {

    List<BusinessHour> findByRestaurantIdOrderByDayOfWeek(UUID restaurantId);

    Optional<BusinessHour> findByRestaurantIdAndDayOfWeek(UUID restaurantId, DayOfWeek dayOfWeek);

    void deleteByRestaurantId(UUID restaurantId);
}

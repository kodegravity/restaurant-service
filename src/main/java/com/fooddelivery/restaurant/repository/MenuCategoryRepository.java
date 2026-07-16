package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {

    List<MenuCategory> findByRestaurantIdOrderByDisplayOrderAsc(UUID restaurantId);

    List<MenuCategory> findByRestaurantIdAndActiveTrueOrderByDisplayOrderAsc(UUID restaurantId);

    Optional<MenuCategory> findByIdAndRestaurantId(UUID id, UUID restaurantId);

    boolean existsByIdAndRestaurantId(UUID id, UUID restaurantId);
}

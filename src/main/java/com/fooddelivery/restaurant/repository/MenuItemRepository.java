package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    List<MenuItem> findByRestaurantIdOrderByDisplayOrderAsc(UUID restaurantId);

    List<MenuItem> findByCategoryIdOrderByDisplayOrderAsc(UUID categoryId);

    @Query("SELECT mi FROM MenuItem mi WHERE mi.categoryId IN " +
           "(SELECT mc.id FROM MenuCategory mc WHERE mc.restaurantId = :restaurantId AND mc.active = true) " +
           "AND mi.available = true ORDER BY mi.categoryId, mi.displayOrder")
    List<MenuItem> findAvailableMenuItemsByRestaurantId(@Param("restaurantId") UUID restaurantId);

    Optional<MenuItem> findByIdAndRestaurantId(UUID id, UUID restaurantId);

    @Query("SELECT mi FROM MenuItem mi WHERE mi.id IN :ids")
    List<MenuItem> findByIdIn(@Param("ids") Set<UUID> ids);

    boolean existsByIdAndRestaurantId(UUID id, UUID restaurantId);
}

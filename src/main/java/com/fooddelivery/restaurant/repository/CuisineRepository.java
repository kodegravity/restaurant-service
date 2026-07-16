package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.entity.Cuisine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface CuisineRepository extends JpaRepository<Cuisine, UUID> {

    List<Cuisine> findByActiveTrue();

    Optional<Cuisine> findByName(String name);

    List<Cuisine> findByIdIn(Set<UUID> ids);
}

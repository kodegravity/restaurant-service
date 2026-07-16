package com.fooddelivery.restaurant.specification;

import com.fooddelivery.restaurant.entity.Cuisine;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.entity.RestaurantAddress;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class RestaurantSpecification {

    public static Specification<Restaurant> hasCity(String city) {
        return (root, query, cb) -> {
            if (city == null || city.isBlank()) {
                return null;
            }
            Join<Restaurant, RestaurantAddress> addressJoin = root.join("address", JoinType.LEFT);
            return cb.equal(cb.lower(addressJoin.get("city")), city.toLowerCase());
        };
    }

    public static Specification<Restaurant> hasPostalCode(String postalCode) {
        return (root, query, cb) -> {
            if (postalCode == null || postalCode.isBlank()) {
                return null;
            }
            Join<Restaurant, RestaurantAddress> addressJoin = root.join("address", JoinType.LEFT);
            return cb.equal(addressJoin.get("postalCode"), postalCode);
        };
    }

    public static Specification<Restaurant> hasCuisine(UUID cuisineId) {
        return (root, query, cb) -> {
            if (cuisineId == null) {
                return null;
            }
            Join<Restaurant, Cuisine> cuisineJoin = root.join("cuisines", JoinType.INNER);
            return cb.equal(cuisineJoin.get("id"), cuisineId);
        };
    }

    public static Specification<Restaurant> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Restaurant> isAcceptingOrders(Boolean acceptingOrders) {
        return (root, query, cb) -> {
            if (acceptingOrders == null) {
                return null;
            }
            return cb.equal(root.get("acceptingOrders"), acceptingOrders);
        };
    }

    public static Specification<Restaurant> isActive() {
        return (root, query, cb) -> cb.equal(root.get("status"), "ACTIVE");
    }
}

package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.request.*;
import com.fooddelivery.restaurant.dto.response.RestaurantResponse;
import com.fooddelivery.restaurant.dto.response.RestaurantSummaryResponse;
import com.fooddelivery.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Restaurant", description = "Restaurant management APIs")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    @Operation(summary = "Create a new restaurant")
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request,
            Principal principal) {
        
        UUID ownerId = UUID.fromString(principal.getName());
        RestaurantResponse response = restaurantService.createRestaurant(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Search restaurants")
    public ResponseEntity<Page<RestaurantSummaryResponse>> searchRestaurants(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String postalCode,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean acceptingOrders,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort) {

        int pageSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sort));

        Page<RestaurantSummaryResponse> restaurants = restaurantService.searchRestaurants(
                city, postalCode, cuisine, name, acceptingOrders, pageable);

        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/{restaurantId}")
    @Operation(summary = "Get restaurant details")
    public ResponseEntity<RestaurantResponse> getRestaurant(@PathVariable UUID restaurantId) {
        RestaurantResponse response = restaurantService.getRestaurant(restaurantId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{restaurantId}")
    @Operation(summary = "Update restaurant")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody UpdateRestaurantRequest request,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        RestaurantResponse response = restaurantService.updateRestaurant(restaurantId, request, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{restaurantId}/status")
    @Operation(summary = "Update restaurant status")
    public ResponseEntity<RestaurantResponse> updateRestaurantStatus(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody UpdateRestaurantStatusRequest request,
            Principal principal,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {

        UUID userId = UUID.fromString(principal.getName());
        boolean isAdmin = roles != null && roles.contains("ADMIN");
        
        RestaurantResponse response = restaurantService.updateRestaurantStatus(restaurantId, request, userId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{restaurantId}/accepting-orders")
    @Operation(summary = "Update accepting orders status")
    public ResponseEntity<RestaurantResponse> updateAcceptingOrders(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody UpdateAcceptingOrdersRequest request,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        RestaurantResponse response = restaurantService.updateAcceptingOrders(restaurantId, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{restaurantId}")
    @Operation(summary = "Delete restaurant")
    public ResponseEntity<Void> deleteRestaurant(
            @PathVariable UUID restaurantId,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        restaurantService.deleteRestaurant(restaurantId, userId);
        return ResponseEntity.noContent().build();
    }
}

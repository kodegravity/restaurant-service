package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.response.RestaurantMenuResponse;
import com.fooddelivery.restaurant.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/menu")
@RequiredArgsConstructor
@Tag(name = "Menu", description = "Public menu browsing APIs")
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    @Operation(summary = "Get restaurant menu for customers")
    public ResponseEntity<RestaurantMenuResponse> getRestaurantMenu(@PathVariable UUID restaurantId) {
        RestaurantMenuResponse response = menuService.getRestaurantMenu(restaurantId);
        return ResponseEntity.ok(response);
    }
}

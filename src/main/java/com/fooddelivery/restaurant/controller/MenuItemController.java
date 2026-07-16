package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.request.CreateMenuItemRequest;
import com.fooddelivery.restaurant.dto.request.UpdateMenuItemAvailabilityRequest;
import com.fooddelivery.restaurant.dto.request.UpdateMenuItemRequest;
import com.fooddelivery.restaurant.dto.response.MenuItemResponse;
import com.fooddelivery.restaurant.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/menu-items")
@RequiredArgsConstructor
@Tag(name = "Menu Item", description = "Menu item management APIs")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping
    @Operation(summary = "Create menu item")
    public ResponseEntity<MenuItemResponse> createMenuItem(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody CreateMenuItemRequest request,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        MenuItemResponse response = menuItemService.createMenuItem(restaurantId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get menu items")
    public ResponseEntity<List<MenuItemResponse>> getMenuItems(
            @PathVariable UUID restaurantId,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        List<MenuItemResponse> response = menuItemService.getMenuItems(restaurantId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{menuItemId}")
    @Operation(summary = "Get menu item")
    public ResponseEntity<MenuItemResponse> getMenuItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID menuItemId) {

        MenuItemResponse response = menuItemService.getMenuItem(restaurantId, menuItemId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{menuItemId}")
    @Operation(summary = "Update menu item")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID menuItemId,
            @Valid @RequestBody UpdateMenuItemRequest request,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        MenuItemResponse response = menuItemService.updateMenuItem(restaurantId, menuItemId, request, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{menuItemId}/availability")
    @Operation(summary = "Update menu item availability")
    public ResponseEntity<MenuItemResponse> updateMenuItemAvailability(
            @PathVariable UUID restaurantId,
            @PathVariable UUID menuItemId,
            @Valid @RequestBody UpdateMenuItemAvailabilityRequest request,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        MenuItemResponse response = menuItemService.updateMenuItemAvailability(restaurantId, menuItemId, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{menuItemId}")
    @Operation(summary = "Delete menu item")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID menuItemId,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        menuItemService.deleteMenuItem(restaurantId, menuItemId, userId);
        return ResponseEntity.noContent().build();
    }
}

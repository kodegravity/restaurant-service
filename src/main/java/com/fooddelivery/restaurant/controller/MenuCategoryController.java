package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.request.CreateMenuCategoryRequest;
import com.fooddelivery.restaurant.dto.request.UpdateCategoryAvailabilityRequest;
import com.fooddelivery.restaurant.dto.request.UpdateMenuCategoryRequest;
import com.fooddelivery.restaurant.dto.response.MenuCategoryResponse;
import com.fooddelivery.restaurant.service.MenuCategoryService;
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
@RequestMapping("/api/v1/restaurants/{restaurantId}/menu-categories")
@RequiredArgsConstructor
@Tag(name = "Menu Category", description = "Menu category management APIs")
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;

    @PostMapping
    @Operation(summary = "Create menu category")
    public ResponseEntity<MenuCategoryResponse> createMenuCategory(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody CreateMenuCategoryRequest request,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        MenuCategoryResponse response = menuCategoryService.createMenuCategory(restaurantId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get menu categories")
    public ResponseEntity<List<MenuCategoryResponse>> getMenuCategories(
            @PathVariable UUID restaurantId,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        List<MenuCategoryResponse> response = menuCategoryService.getMenuCategories(restaurantId, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update menu category")
    public ResponseEntity<MenuCategoryResponse> updateMenuCategory(
            @PathVariable UUID restaurantId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateMenuCategoryRequest request,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        MenuCategoryResponse response = menuCategoryService.updateMenuCategory(restaurantId, categoryId, request, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{categoryId}/availability")
    @Operation(summary = "Update category availability")
    public ResponseEntity<MenuCategoryResponse> updateCategoryAvailability(
            @PathVariable UUID restaurantId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryAvailabilityRequest request,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        MenuCategoryResponse response = menuCategoryService.updateCategoryAvailability(restaurantId, categoryId, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete menu category")
    public ResponseEntity<Void> deleteMenuCategory(
            @PathVariable UUID restaurantId,
            @PathVariable UUID categoryId,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        menuCategoryService.deleteMenuCategory(restaurantId, categoryId, userId);
        return ResponseEntity.noContent().build();
    }
}

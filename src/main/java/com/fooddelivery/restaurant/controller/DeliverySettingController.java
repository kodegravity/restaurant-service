package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.request.UpdateDeliverySettingRequest;
import com.fooddelivery.restaurant.dto.response.DeliverySettingResponse;
import com.fooddelivery.restaurant.service.DeliverySettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/delivery-settings")
@RequiredArgsConstructor
@Tag(name = "Delivery Settings", description = "Delivery settings management APIs")
public class DeliverySettingController {

    private final DeliverySettingService deliverySettingService;

    @GetMapping
    @Operation(summary = "Get delivery settings")
    public ResponseEntity<DeliverySettingResponse> getDeliverySettings(@PathVariable UUID restaurantId) {
        DeliverySettingResponse response = deliverySettingService.getDeliverySettings(restaurantId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(summary = "Update delivery settings")
    public ResponseEntity<DeliverySettingResponse> updateDeliverySettings(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody UpdateDeliverySettingRequest request,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        DeliverySettingResponse response = deliverySettingService.updateDeliverySettings(restaurantId, request, userId);
        return ResponseEntity.ok(response);
    }
}

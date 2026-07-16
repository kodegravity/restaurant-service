package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.request.UpdateBusinessHoursRequest;
import com.fooddelivery.restaurant.dto.response.BusinessHourResponse;
import com.fooddelivery.restaurant.service.BusinessHourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/business-hours")
@RequiredArgsConstructor
@Tag(name = "Business Hours", description = "Business hours management APIs")
public class BusinessHourController {

    private final BusinessHourService businessHourService;

    @GetMapping
    @Operation(summary = "Get business hours")
    public ResponseEntity<List<BusinessHourResponse>> getBusinessHours(@PathVariable UUID restaurantId) {
        List<BusinessHourResponse> response = businessHourService.getBusinessHours(restaurantId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(summary = "Update business hours")
    public ResponseEntity<List<BusinessHourResponse>> updateBusinessHours(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody UpdateBusinessHoursRequest request,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        List<BusinessHourResponse> response = businessHourService.updateBusinessHours(restaurantId, request, userId);
        return ResponseEntity.ok(response);
    }
}

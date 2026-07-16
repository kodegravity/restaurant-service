package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.request.ValidateOrderRequest;
import com.fooddelivery.restaurant.dto.response.ValidateOrderResponse;
import com.fooddelivery.restaurant.service.OrderValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/restaurants/{restaurantId}")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal Order Validation", description = "Internal APIs for order validation")
public class InternalOrderValidationController {

    private final OrderValidationService orderValidationService;

    @PostMapping("/validate-order")
    @Operation(summary = "Validate order (Internal API - requires SERVICE role)")
    public ResponseEntity<ValidateOrderResponse> validateOrder(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody ValidateOrderRequest request) {

        log.info("Received order validation request for restaurant: {}", restaurantId);
        ValidateOrderResponse response = orderValidationService.validateOrder(restaurantId, request);
        return ResponseEntity.ok(response);
    }
}

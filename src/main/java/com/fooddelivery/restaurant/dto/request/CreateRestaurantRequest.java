package com.fooddelivery.restaurant.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    @Size(max = 150, message = "Restaurant name must not exceed 150 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    private String phone;

    @Email(message = "Email must be valid")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a valid 3-letter code")
    private String currency;

    @NotBlank(message = "Timezone is required")
    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    @Min(value = 1, message = "Estimated preparation time must be at least 1 minute")
    private Integer estimatedPreparationMinutes;

    @Valid
    @NotNull(message = "Address is required")
    private AddressRequest address;

    private Set<String> cuisineIds;
}

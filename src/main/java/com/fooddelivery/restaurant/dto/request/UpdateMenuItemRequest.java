package com.fooddelivery.restaurant.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMenuItemRequest {

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    @NotBlank(message = "Menu item name is required")
    @Size(max = 150, message = "Menu item name must not exceed 150 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a valid 3-letter code")
    private String currency;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @NotNull(message = "Available status is required")
    private Boolean available;

    @NotNull(message = "Vegetarian status is required")
    private Boolean vegetarian;

    @NotNull(message = "Vegan status is required")
    private Boolean vegan;

    @NotNull(message = "Gluten-free status is required")
    private Boolean glutenFree;

    @Min(value = 0, message = "Spicy level must be between 0 and 5")
    @Max(value = 5, message = "Spicy level must be between 0 and 5")
    private Integer spicyLevel;

    @Min(value = 1, message = "Preparation time must be at least 1 minute")
    private Integer preparationMinutes;

    @Min(value = 0, message = "Display order must not be negative")
    private Integer displayOrder;
}

package com.fooddelivery.restaurant.exception;

import java.util.UUID;

public class MenuCategoryNotFoundException extends RuntimeException {
    public MenuCategoryNotFoundException(UUID id) {
        super("Menu category not found with id: " + id);
    }
}

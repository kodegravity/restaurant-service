package io.codeqube.restaurantservice.controller;

import io.codeqube.restaurantservice.dto.MenuItemRequest;
import io.codeqube.restaurantservice.model.MenuItem;
import io.codeqube.restaurantservice.service.MenuItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

    private final MenuItemService service;

    public MenuItemController(MenuItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<MenuItem> getAll(@RequestParam(required = false) String category,
                                  @RequestParam(required = false) Boolean available) {
        if (Boolean.TRUE.equals(available)) return service.getAvailable();
        if (category != null) return service.getByCategory(category);
        return service.getAll();
    }

    @GetMapping("/{id}")
    public MenuItem getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<MenuItem> create(@RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public MenuItem update(@PathVariable Long id, @RequestBody MenuItemRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

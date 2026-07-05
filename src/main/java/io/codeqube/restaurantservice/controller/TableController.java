package io.codeqube.restaurantservice.controller;

import io.codeqube.restaurantservice.dto.TableRequest;
import io.codeqube.restaurantservice.model.RestaurantTable;
import io.codeqube.restaurantservice.service.TableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final TableService service;

    public TableController(TableService service) {
        this.service = service;
    }

    @GetMapping
    public List<RestaurantTable> getAll(@RequestParam(required = false) String status) {
        if (status != null) return service.getByStatus(status);
        return service.getAll();
    }

    @GetMapping("/{id}")
    public RestaurantTable getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<RestaurantTable> create(@RequestBody TableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public RestaurantTable update(@PathVariable Long id, @RequestBody TableRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

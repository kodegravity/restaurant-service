package io.codeqube.restaurantservice.service;

import io.codeqube.restaurantservice.dto.MenuItemRequest;
import io.codeqube.restaurantservice.exception.ResourceNotFoundException;
import io.codeqube.restaurantservice.model.MenuItem;
import io.codeqube.restaurantservice.repository.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuItemService {

    private final MenuItemRepository repository;

    public MenuItemService(MenuItemRepository repository) {
        this.repository = repository;
    }

    public List<MenuItem> getAll() { return repository.findAll(); }

    public List<MenuItem> getAvailable() { return repository.findByAvailableTrue(); }

    public List<MenuItem> getByCategory(String category) { return repository.findByCategory(category); }

    public MenuItem getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
    }

    public MenuItem create(MenuItemRequest req) {
        MenuItem item = new MenuItem();
        item.setName(req.getName());
        item.setDescription(req.getDescription());
        item.setPrice(req.getPrice());
        item.setCategory(req.getCategory());
        item.setAvailable(req.isAvailable());
        return repository.save(item);
    }

    public MenuItem update(Long id, MenuItemRequest req) {
        MenuItem item = getById(id);
        item.setName(req.getName());
        item.setDescription(req.getDescription());
        item.setPrice(req.getPrice());
        item.setCategory(req.getCategory());
        item.setAvailable(req.isAvailable());
        return repository.save(item);
    }

    public void delete(Long id) {
        getById(id);
        repository.deleteById(id);
    }
}

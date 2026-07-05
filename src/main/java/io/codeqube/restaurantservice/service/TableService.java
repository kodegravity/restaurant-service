package io.codeqube.restaurantservice.service;

import io.codeqube.restaurantservice.dto.TableRequest;
import io.codeqube.restaurantservice.exception.BadRequestException;
import io.codeqube.restaurantservice.exception.ResourceNotFoundException;
import io.codeqube.restaurantservice.model.RestaurantTable;
import io.codeqube.restaurantservice.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TableService {

    private final RestaurantTableRepository repository;

    public TableService(RestaurantTableRepository repository) {
        this.repository = repository;
    }

    public List<RestaurantTable> getAll() { return repository.findAll(); }

    public List<RestaurantTable> getByStatus(String status) {
        return repository.findByStatus(parseStatus(status));
    }

    public RestaurantTable getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));
    }

    public RestaurantTable create(TableRequest req) {
        if (repository.findByTableNumber(req.getTableNumber()).isPresent()) {
            throw new BadRequestException("Table number " + req.getTableNumber() + " already exists");
        }
        RestaurantTable table = new RestaurantTable();
        table.setTableNumber(req.getTableNumber());
        table.setCapacity(req.getCapacity());
        if (req.getStatus() != null) table.setStatus(parseStatus(req.getStatus()));
        return repository.save(table);
    }

    public RestaurantTable update(Long id, TableRequest req) {
        RestaurantTable table = getById(id);
        if (req.getCapacity() != null) table.setCapacity(req.getCapacity());
        if (req.getStatus() != null) table.setStatus(parseStatus(req.getStatus()));
        return repository.save(table);
    }

    public void delete(Long id) {
        getById(id);
        repository.deleteById(id);
    }

    private RestaurantTable.Status parseStatus(String status) {
        try {
            return RestaurantTable.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid table status: " + status);
        }
    }
}

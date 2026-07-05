package io.codeqube.restaurantservice.service;

import io.codeqube.restaurantservice.dto.OrderRequest;
import io.codeqube.restaurantservice.exception.BadRequestException;
import io.codeqube.restaurantservice.exception.ResourceNotFoundException;
import io.codeqube.restaurantservice.model.*;
import io.codeqube.restaurantservice.repository.MenuItemRepository;
import io.codeqube.restaurantservice.repository.OrderRepository;
import io.codeqube.restaurantservice.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantTableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;

    public OrderService(OrderRepository orderRepository,
                        RestaurantTableRepository tableRepository,
                        MenuItemRepository menuItemRepository) {
        this.orderRepository = orderRepository;
        this.tableRepository = tableRepository;
        this.menuItemRepository = menuItemRepository;
    }

    public List<Order> getAll() { return orderRepository.findAll(); }

    public List<Order> getByStatus(String status) {
        return orderRepository.findByStatus(parseStatus(status));
    }

    public List<Order> getByTable(Long tableId) {
        return orderRepository.findByTableId(tableId);
    }

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Transactional
    public Order create(OrderRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }

        RestaurantTable table = tableRepository.findById(req.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + req.getTableId()));

        Order order = new Order();
        order.setTable(table);

        for (OrderRequest.OrderItemRequest itemReq : req.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + itemReq.getMenuItemId()));

            if (!menuItem.isAvailable()) {
                throw new BadRequestException("Menu item '" + menuItem.getName() + "' is not available");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setUnitPrice(menuItem.getPrice());
            order.getItems().add(orderItem);
        }

        table.setStatus(RestaurantTable.Status.OCCUPIED);
        tableRepository.save(table);

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(Long id, String status) {
        Order order = getById(id);
        Order.Status newStatus = parseStatus(status);
        order.setStatus(newStatus);

        if (newStatus == Order.Status.COMPLETED || newStatus == Order.Status.CANCELLED) {
            boolean hasActiveOrders = orderRepository.findByTableId(order.getTable().getId())
                    .stream()
                    .anyMatch(o -> !o.getId().equals(id)
                            && o.getStatus() != Order.Status.COMPLETED
                            && o.getStatus() != Order.Status.CANCELLED);
            if (!hasActiveOrders) {
                order.getTable().setStatus(RestaurantTable.Status.AVAILABLE);
                tableRepository.save(order.getTable());
            }
        }

        return orderRepository.save(order);
    }

    private Order.Status parseStatus(String status) {
        try {
            return Order.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: " + status);
        }
    }
}

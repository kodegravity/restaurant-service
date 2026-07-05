package io.codeqube.restaurantservice.repository;

import io.codeqube.restaurantservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(Order.Status status);
    List<Order> findByTableId(Long tableId);
}

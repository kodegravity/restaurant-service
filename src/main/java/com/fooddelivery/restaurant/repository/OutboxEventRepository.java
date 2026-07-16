package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.entity.OutboxEvent;
import com.fooddelivery.restaurant.entity.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status ORDER BY o.createdAt ASC LIMIT :limit")
    List<OutboxEvent> findPendingEventsForProcessing(@Param("status") OutboxEventStatus status, @Param("limit") int limit);

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxEventStatus status);

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'FAILED' AND o.retryCount < :maxRetries ORDER BY o.createdAt ASC")
    List<OutboxEvent> findFailedEventsForRetry(@Param("maxRetries") int maxRetries);
}

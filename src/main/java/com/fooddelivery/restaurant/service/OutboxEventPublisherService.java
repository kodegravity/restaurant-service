package com.fooddelivery.restaurant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.restaurant.entity.OutboxEvent;
import com.fooddelivery.restaurant.entity.OutboxEventStatus;
import com.fooddelivery.restaurant.event.model.RestaurantEventEnvelope;
import com.fooddelivery.restaurant.event.producer.RestaurantEventPublisher;
import com.fooddelivery.restaurant.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final RestaurantEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${application.outbox.batch-size:100}")
    private int batchSize;

    @Value("${application.outbox.max-retries:5}")
    private int maxRetries;

    @Scheduled(fixedDelayString = "${application.outbox.polling-interval:5000}")
    public void publishPendingEvents() {
        try {
            List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEventsForProcessing(
                    OutboxEventStatus.PENDING, batchSize);

            if (!pendingEvents.isEmpty()) {
                log.info("Found {} pending outbox events to publish", pendingEvents.size());
                
                for (OutboxEvent event : pendingEvents) {
                    publishEvent(event);
                }
            }
        } catch (Exception e) {
            log.error("Error processing outbox events", e);
        }
    }

    @Transactional
    public void publishEvent(OutboxEvent outboxEvent) {
        try {
            RestaurantEventEnvelope envelope = RestaurantEventEnvelope.builder()
                    .eventId(outboxEvent.getId())
                    .eventType(outboxEvent.getEventType())
                    .aggregateType(outboxEvent.getAggregateType())
                    .aggregateId(outboxEvent.getAggregateId())
                    .occurredAt(outboxEvent.getCreatedAt())
                    .version(1)
                    .correlationId(UUID.randomUUID())
                    .payload(objectMapper.readValue(outboxEvent.getPayload(), Object.class))
                    .build();

            eventPublisher.publish(envelope);

            outboxEvent.setStatus(OutboxEventStatus.PUBLISHED);
            outboxEvent.setPublishedAt(OffsetDateTime.now());
            outboxEventRepository.save(outboxEvent);

            log.info("Published outbox event: {} type: {}", outboxEvent.getId(), outboxEvent.getEventType());

        } catch (JsonProcessingException e) {
            log.error("Failed to parse event payload for outbox event: {}", outboxEvent.getId(), e);
            handleEventFailure(outboxEvent, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to publish outbox event: {}", outboxEvent.getId(), e);
            handleEventFailure(outboxEvent, e.getMessage());
        }
    }

    @Transactional
    public void handleEventFailure(OutboxEvent event, String errorMessage) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setLastError(errorMessage != null && errorMessage.length() > 1000 ? 
                errorMessage.substring(0, 1000) : errorMessage);

        if (event.getRetryCount() >= maxRetries) {
            event.setStatus(OutboxEventStatus.FAILED);
            log.error("Outbox event {} failed after {} retries", event.getId(), maxRetries);
        }

        outboxEventRepository.save(event);
    }

    @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
    public void retryFailedEvents() {
        try {
            List<OutboxEvent> failedEvents = outboxEventRepository.findFailedEventsForRetry(maxRetries);
            
            if (!failedEvents.isEmpty()) {
                log.info("Retrying {} failed outbox events", failedEvents.size());
                
                for (OutboxEvent event : failedEvents) {
                    event.setStatus(OutboxEventStatus.PENDING);
                    outboxEventRepository.save(event);
                }
            }
        } catch (Exception e) {
            log.error("Error retrying failed outbox events", e);
        }
    }
}

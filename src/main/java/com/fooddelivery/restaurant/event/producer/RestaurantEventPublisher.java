package com.fooddelivery.restaurant.event.producer;

import com.fooddelivery.restaurant.event.model.RestaurantEventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestaurantEventPublisher {

    private final KafkaTemplate<String, RestaurantEventEnvelope> kafkaTemplate;

    @Value("${application.kafka.topic-name:restaurant-events}")
    private String topicName;

    public void publish(RestaurantEventEnvelope event) {
        String key = event.getAggregateId().toString();
        
        log.info("Publishing event {} for aggregate {}", event.getEventType(), event.getAggregateId());
        
        CompletableFuture<SendResult<String, RestaurantEventEnvelope>> future = kafkaTemplate.send(topicName, key, event);
        
        future.whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Failed to publish event {} for aggregate {}: {}", 
                        event.getEventType(), event.getAggregateId(), exception.getMessage(), exception);
            } else {
                log.info("Successfully published event {} for aggregate {} to partition {}",
                        event.getEventType(), event.getAggregateId(), result.getRecordMetadata().partition());
            }
        });
    }
}

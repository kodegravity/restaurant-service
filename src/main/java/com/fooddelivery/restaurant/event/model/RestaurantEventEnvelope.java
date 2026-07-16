package com.fooddelivery.restaurant.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantEventEnvelope {

    private UUID eventId;
    private String eventType;
    private String aggregateType;
    private UUID aggregateId;
    private OffsetDateTime occurredAt;
    private Integer version;
    private UUID correlationId;
    private Object payload;
}

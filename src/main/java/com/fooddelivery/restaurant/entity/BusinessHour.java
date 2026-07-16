package com.fooddelivery.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "business_hour", schema = "restaurant_service",
       uniqueConstraints = @UniqueConstraint(name = "uk_business_hour_restaurant_day", 
                                             columnNames = {"restaurant_id", "day_of_week"}),
       indexes = @Index(name = "idx_business_hour_restaurant_day", columnList = "restaurant_id, day_of_week"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessHour {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 10, nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "closed", nullable = false)
    @Builder.Default
    private Boolean closed = false;
}

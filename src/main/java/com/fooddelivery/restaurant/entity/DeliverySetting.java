package com.fooddelivery.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_setting", schema = "restaurant_service",
       uniqueConstraints = @UniqueConstraint(name = "uk_delivery_setting_restaurant", columnNames = "restaurant_id"))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliverySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "restaurant_id", nullable = false, unique = true)
    private Restaurant restaurant;

    @Column(name = "delivery_radius_km", precision = 5, scale = 2)
    private BigDecimal deliveryRadiusKm;

    @Column(name = "minimum_order_amount", precision = 10, scale = 2)
    private BigDecimal minimumOrderAmount;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "free_delivery_threshold", precision = 10, scale = 2)
    private BigDecimal freeDeliveryThreshold;

    @Column(name = "estimated_delivery_minutes")
    private Integer estimatedDeliveryMinutes;

    @Column(name = "delivery_enabled", nullable = false)
    @Builder.Default
    private Boolean deliveryEnabled = true;

    @Column(name = "pickup_enabled", nullable = false)
    @Builder.Default
    private Boolean pickupEnabled = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}

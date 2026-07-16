package com.fooddelivery.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "restaurant", schema = "restaurant_service", indexes = {
        @Index(name = "idx_restaurant_owner_id", columnList = "owner_id"),
        @Index(name = "idx_restaurant_status", columnList = "status"),
        @Index(name = "idx_restaurant_accepting_orders", columnList = "accepting_orders")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private RestaurantStatus status = RestaurantStatus.DRAFT;

    @Column(name = "accepting_orders", nullable = false)
    @Builder.Default
    private Boolean acceptingOrders = false;

    @Column(name = "minimum_order_amount", precision = 10, scale = 2)
    private BigDecimal minimumOrderAmount;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "estimated_preparation_minutes")
    private Integer estimatedPreparationMinutes;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Version
    @Column(name = "version")
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToOne(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private RestaurantAddress address;

    @ManyToMany
    @JoinTable(
            name = "restaurant_cuisine",
            schema = "restaurant_service",
            joinColumns = @JoinColumn(name = "restaurant_id"),
            inverseJoinColumns = @JoinColumn(name = "cuisine_id"),
            indexes = {
                    @Index(name = "idx_restaurant_cuisine_restaurant_id", columnList = "restaurant_id"),
                    @Index(name = "idx_restaurant_cuisine_cuisine_id", columnList = "cuisine_id")
            }
    )
    @Builder.Default
    private Set<Cuisine> cuisines = new HashSet<>();

    @OneToOne(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private DeliverySetting deliverySetting;

    public void setAddress(RestaurantAddress address) {
        this.address = address;
        if (address != null) {
            address.setRestaurant(this);
        }
    }

    public void setDeliverySetting(DeliverySetting deliverySetting) {
        this.deliverySetting = deliverySetting;
        if (deliverySetting != null) {
            deliverySetting.setRestaurant(this);
        }
    }
}

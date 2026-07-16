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
@Table(name = "menu_item", schema = "restaurant_service", indexes = {
        @Index(name = "idx_menu_item_restaurant_available", columnList = "restaurant_id, available"),
        @Index(name = "idx_menu_item_category_available", columnList = "category_id, available"),
        @Index(name = "idx_menu_item_name", columnList = "name")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "available", nullable = false)
    @Builder.Default
    private Boolean available = true;

    @Column(name = "vegetarian", nullable = false)
    @Builder.Default
    private Boolean vegetarian = false;

    @Column(name = "vegan", nullable = false)
    @Builder.Default
    private Boolean vegan = false;

    @Column(name = "gluten_free", nullable = false)
    @Builder.Default
    private Boolean glutenFree = false;

    @Column(name = "spicy_level")
    private Integer spicyLevel;

    @Column(name = "preparation_minutes")
    private Integer preparationMinutes;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Version
    @Column(name = "version")
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}

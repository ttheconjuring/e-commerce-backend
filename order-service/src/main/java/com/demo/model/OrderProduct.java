package com.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a single line item within an {@link Order}.
 * <p>
 * This entity links an {@link Order} to a specific product (by its ID)
 * and stores the quantity and price for that product at the time of purchase.
 * It is part of the {@link Order} aggregate root and is managed
 * via {@link CascadeType#ALL} from the {@link Order} entity.
 */
@Entity
@Table(name = "order_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderProduct {

    /**
     * The unique identifier (Primary Key) for this specific order line item.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * A many-to-one relationship linking this line item back to its
     * parent {@link Order}.
     * <p>
     * This is the "owning" side of the relationship and is fetched lazily
     * to avoid loading the entire order when only the line item is needed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * The unique identifier of the product in the Product Service.
     * <p>
     * This is a logical link (a "soft" foreign key) and not a hard
     * database constraint, as the actual product data lives in a
     * separate microservice.
     */
    // This is a logical link to the Product service, not a hard FK constraint.
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    /**
     * The number of units of this product purchased in this order.
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * The price of a single unit of this product *at the time of purchase*.
     * <p>
     * Stored as {@link BigDecimal} for financial precision. This is a
     * snapshot of the price and is crucial for historical accuracy, as the
     * product's price in the Product Service may change later.
     */
    @Column(name = "price_per_unit", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerUnit;

}

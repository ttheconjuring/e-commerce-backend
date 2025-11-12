package com.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a Product, the central aggregate root for the Product Service.
 * <p>
 * This entity tracks all product information, including its description,
 * price, and most importantly, the current inventory level
 * ({@code stockQuantity}). Its stock is managed by the
 * {@link com.demo.service.ProductService}.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /**
     * The unique identifier (Primary Key) for the product.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The name of the product. This field must be non-null and unique.
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * A detailed description of the product.
     */
    @Column(length = 1000)
    private String description;

    /**
     * The selling price of the product.
     * Stored as {@link BigDecimal} for financial precision.
     */
    @Column(nullable = false)
    private BigDecimal price;

    /**
     * The three-letter ISO currency code (e.g., "USD") for the price.
     */
    @Column(nullable = false, length = 3)
    private String currency;

    /**
     * The current available inventory (stock) for this product.
     * <p>
     * This is the critical field used by the
     * {@link com.demo.service.ProductService#confirmAvailability(java.util.List)}
     * to validate an order.
     */
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    /**
     * Timestamp of when this product was first created.
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdDate;

    /**
     * Timestamp of the last update to this product .
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedDate;

    /**
     * A set of {@link Category} entities associated with this product.
     * <p>
     * This is the "owning" side of the many-to-many relationship, and it
     * defines the join table {@code products_categories}.
     * Cascading is set to PERSIST and MERGE, so creating or updating a
     * product will cascade those operations to its associated categories
     * (e.g., when adding a product to a new, un-persisted category).
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "products_categories",
            joinColumns = @JoinColumn(name = "product_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "id"))
    private Set<Category> categories;

}

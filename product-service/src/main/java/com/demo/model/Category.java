package com.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

/**
 * Represents a product category (e.g., "Electronics", "Books").
 * <p>
 * This entity has a many-to-many relationship with the {@link Product}
 * entity, allowing a product to belong to multiple categories and a
 * category to contain multiple products.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    /**
     * The unique identifier (Primary Key) for the category.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The name of the category.
     * This field must be non-null and unique.
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * The set of products associated with this category.
     * <p>
     * This is the "inverse" side of the many-to-many relationship.
     * The "owning" side is defined in the {@link Product} entity's
     * {@code categories} field (which specifies the join table).
     */
    @ManyToMany(mappedBy = "categories")
    private Set<Product> products;

}

package com.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Represents a physical shipping address.
 * <p>
 * This entity is used to store address details, which are linked to
 * an {@link Order}. It is managed by the {@link com.demo.service.OrderService}
 * and saved as part of the order creation process.
 */
@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    /**
     * The unique internal identifier (Primary Key) for the address.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The street address line (e.g., "123 Main St").
     */
    @Column(name = "street_address", nullable = false)
    private String address;

    /**
     * The postal or ZIP code.
     */
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    /**
     * The city name.
     */
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    /**
     * The country name.
     */
    @Column(name = "country", nullable = false, length = 50)
    private String country;

}

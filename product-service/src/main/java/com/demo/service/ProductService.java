package com.demo.service;

import com.demo.common.dto.InsufficientProductDTO;
import com.demo.common.dto.ProductQuantityDTO;

import java.util.List;

/**
 * Service interface for the core business logic of the Product Service.
 * <p>
 * This contract defines operations for managing the product catalog (seeding)
 * and, most importantly, the inventory (checking availability and updating stock).
 * It is the primary service used by the
 * {@link com.demo.component.ProductCommandsHandler}.
 */
public interface ProductService {

    /**
     * A utility method to populate the database with initial sample
     * data (products and categories) for demonstration or testing purposes.
     */
    void seedProducts();

    /**
     * Checks if the requested quantities for a list of products are currently in stock.
     * <p>
     * This method *validates* availability but does not place a hold or "reserve"
     * the items. This is the "check" step in a "check-then-act" pattern.
     *
     * @param productsToCheck A list of product IDs and their requested quantities.
     * @return A list of {@link InsufficientProductDTO}s detailing which
     * products are out of stock. **Returns {@code null}** if all products
     * are available in the requested quantities.
     */
    List<InsufficientProductDTO> confirmAvailability(List<ProductQuantityDTO> productsToCheck);

    /**
     * Modifies the stock quantity for a list of products.
     * <p>
     * This is the "act" step, serving as the "commit" (decrement) or
     * "compensating" (increment/rollback) phase of the saga.
     *
     * @param productsToUpdate A list of product IDs and the quantities to adjust.
     * @param condition        A string (e.g., "UPDATE_PRODUCTS", "RESTORE_PRODUCTS")
     * that dictates the type of update. "UPDATE"
     * typically decrements stock, while "RESTORE"
     * increments it.
     */
    void updateProductsQuantity(List<ProductQuantityDTO> productsToUpdate, String condition);

}

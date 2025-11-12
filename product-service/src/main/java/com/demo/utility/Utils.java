package com.demo.utility;

import com.demo.common.dto.InsufficientProductDTO;
import com.demo.common.dto.ProductQuantityDTO;
import com.demo.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A generic utility class providing static helper methods for the Product service.
 */
public class Utils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Utils(){}

    /**
     * Converts a list of {@link Product} entities into a list of
     * {@link InsufficientProductDTO}s.
     * <p>
     * This method is used to build a detailed response payload when a stock
     * check fails. It cross-references the list of insufficient products
     * with the original requested quantities to create a DTO that clearly
     * shows both "requested" and "available" amounts.
     *
     * @param insufficientProducts A list of {@link Product} entities that were
     * found to have insufficient stock.
     * @param productsToCheck      The original list of requested products ({@link ProductQuantityDTO})
     * from the command.
     * @return A new list of {@link InsufficientProductDTO}s detailing the shortages.
     */
    public static List<InsufficientProductDTO> convertToInsufficientProductsList(List<Product> insufficientProducts, List<ProductQuantityDTO> productsToCheck) {
        // Map of available quantities for insufficient products
        Map<UUID, Integer> availableProductIdQuantityMap = insufficientProducts.stream().collect(Collectors.toMap(Product::getId, Product::getStockQuantity));
        // Map of requested quantities for all checked products
        Map<UUID, Integer> requestedProductIdQuantityMap = productsToCheck.stream().collect(Collectors.toMap(ProductQuantityDTO::getProductId, ProductQuantityDTO::getQuantity));
        List<InsufficientProductDTO> insufficientProductList = new ArrayList<>();
        // For each insufficient product, create the detailed DTO
        for (Map.Entry<UUID, Integer> product : availableProductIdQuantityMap.entrySet()) {
            InsufficientProductDTO insufficientProduct = new InsufficientProductDTO();
            insufficientProduct.setProductId(product.getKey());
            // Look up the original requested quantity
            insufficientProduct.setRequestedQuantity(requestedProductIdQuantityMap.get(product.getKey()));
            // Set the available quantity
            insufficientProduct.setAvailableQuantity(product.getValue());
            insufficientProductList.add(insufficientProduct);
        }
        return insufficientProductList;
    }

}

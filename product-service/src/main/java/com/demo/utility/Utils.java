package com.demo.utility;

import com.demo.common.dto.InsufficientProductDTO;
import com.demo.common.dto.ProductQuantityDTO;
import com.demo.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Utils {

    private Utils(){
        throw new AssertionError("Utils class should not be instantiated.");
    }

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

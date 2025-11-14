package com.demo.service;

import com.demo.common.constant.Commands;
import com.demo.common.dto.InsufficientProductDTO;
import com.demo.common.dto.ProductQuantityDTO;
import com.demo.model.Category;
import com.demo.model.Product;
import com.demo.repository.ProductRepository;
import com.demo.utility.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public void seedProducts() {
        // --- Create Categories ---
        Category electronics = new Category();
        electronics.setName("Electronics");

        Category books = new Category();
        books.setName("Books");

        Category homeAndKitchen = new Category();
        homeAndKitchen.setName("Home & Kitchen");

        // --- Create Products and Associate Categories ---
        // Product 1: Smart TV (Electronics, Home & Kitchen)
        Product smartTv = new Product();
        smartTv.setName("Smart TV 4K");
        smartTv.setDescription("A 55-inch 4K UHD Smart TV with HDR support and built-in streaming apps.");
        smartTv.setPrice(new BigDecimal("899.99"));
        smartTv.setCurrency("USD");
        smartTv.setStockQuantity(50);
        smartTv.setCreatedDate(Instant.now());
        smartTv.setUpdatedDate(Instant.now());
        smartTv.setCategories(Set.of(electronics, homeAndKitchen));

        // Product 2: Book (Books)
        Product book = new Product();
        book.setName("The Hitchhiker's Guide to the Galaxy");
        book.setDescription("A comedy science fiction series created by Douglas Adams.");
        book.setPrice(new BigDecimal("15.50"));
        book.setCurrency("USD");
        book.setStockQuantity(200);
        book.setCreatedDate(Instant.now());
        book.setUpdatedDate(Instant.now());
        book.setCategories(Set.of(books));

        // Product 3: Headphones (Electronics)
        Product headphones = new Product();
        headphones.setName("Wireless Noise-Cancelling Headphones");
        headphones.setDescription("Over-ear headphones with active noise cancellation and 30-hour battery life.");
        headphones.setPrice(new BigDecimal("249.00"));
        headphones.setCurrency("USD");
        headphones.setStockQuantity(120);
        headphones.setCreatedDate(Instant.now());
        headphones.setUpdatedDate(Instant.now());
        headphones.setCategories(Set.of(electronics));

        // Product 4: Espresso Machine (Home & Kitchen)
        Product espressoMachine = new Product();
        espressoMachine.setName("Espresso Machine");
        espressoMachine.setDescription("A semi-automatic espresso machine for perfect cappuccinos and lattes at home.");
        espressoMachine.setPrice(new BigDecimal("475.95"));
        espressoMachine.setCurrency("USD");
        espressoMachine.setStockQuantity(35);
        espressoMachine.setCreatedDate(Instant.now());
        espressoMachine.setUpdatedDate(Instant.now());
        espressoMachine.setCategories(Set.of(homeAndKitchen));

        // Product 5: Smart Home Hub (Electronics, Home & Kitchen)
        Product smartHub = new Product();
        smartHub.setName("Smart Home Hub");
        smartHub.setDescription("Control your smart home devices including lights, thermostats, and locks with this central hub.");
        smartHub.setPrice(new BigDecimal("99.99"));
        smartHub.setCurrency("USD");
        smartHub.setStockQuantity(80);
        smartHub.setCreatedDate(Instant.now());
        smartHub.setUpdatedDate(Instant.now());
        smartHub.setCategories(Set.of(electronics, homeAndKitchen));

        // --- Save Products to the Database ---
        // Because Product owns the relationship and has CascadeType configured,
        // saving the products will also save the new categories and populate the join table.
        productRepository.saveAll(Set.of(smartTv, book, headphones, espressoMachine, smartHub));
    }

    // TODO: add reserving and releasing products functionality
    public List<InsufficientProductDTO> confirmAvailability(List<ProductQuantityDTO> productsToCheck) {
        // Map for easy quantity lookup
        Map<UUID, Integer> productIdQuantityMap = productsToCheck.stream().collect(Collectors.toMap(ProductQuantityDTO::getProductId, ProductQuantityDTO::getQuantity));
        List<UUID> productIds = List.copyOf(productIdQuantityMap.keySet());
        // Fetch all products in one go
        List<Product> products = this.productRepository.findAllById(productIds);
        // Find products that have insufficient stock
        List<Product> insufficientProducts = products.stream().filter(product -> product.getStockQuantity() < productIdQuantityMap.get(product.getId())).toList();
        if (!insufficientProducts.isEmpty()) {
            // Failure: return list of problems
            return Utils.convertToInsufficientProductsList(insufficientProducts, productsToCheck);
        }
        // Success: return null
        return new ArrayList<>();
    }

    @Transactional
    public void updateProductsQuantity(List<ProductQuantityDTO> productsToUpdate, String condition) {
        switch (condition) {
            case Commands.UPDATE_PRODUCTS -> {
                // Decrement stock (commit)
                for (ProductQuantityDTO productQuantityDTO : productsToUpdate) {
                    Product product = this.productRepository.findById(productQuantityDTO.getProductId()).orElseThrow();
                    product.setStockQuantity(product.getStockQuantity() - productQuantityDTO.getQuantity());
                    this.productRepository.saveAndFlush(product);
                }
            }
            case Commands.RESTORE_PRODUCTS -> {
                // Increment stock (rollback)
                for (ProductQuantityDTO productQuantityDTO : productsToUpdate) {
                    Product product = this.productRepository.findById(productQuantityDTO.getProductId()).orElseThrow();
                    product.setStockQuantity(product.getStockQuantity() + productQuantityDTO.getQuantity());
                    this.productRepository.saveAndFlush(product);
                }
            }
        }
    }

}



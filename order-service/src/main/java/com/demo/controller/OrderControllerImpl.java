package com.demo.controller;

import com.demo.model.Order;
import com.demo.model.dto.CreateOrderRequest;
import com.demo.model.dto.OrderCreatedResponse;
import com.demo.service.impl.OrderServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderControllerImpl implements OrderController {

    private final OrderServiceImpl orderService;

    @PostMapping("/create")
    @Override
    public ResponseEntity<OrderCreatedResponse> createOrder(@RequestBody @Valid CreateOrderRequest createOrderRequest) {
        // Return a 201 CREATED status
        return ResponseEntity.status(HttpStatus.CREATED).body(
                // Delegate creation logic to the service layer
                this.orderService.create(createOrderRequest));
    }

    @GetMapping("/status/{id}")
    @Override
    public ResponseEntity<String> checkOrderStatus(@PathVariable UUID id) {
        // Retrieve the order from the service layer
        Order order = this.orderService.retrieve(id);
        String orderStatus = order.getStatus().toString();

        // Format the response based on the status
        switch (orderStatus) {
            case "PLACED", "COMPLETE" -> {
                return ResponseEntity.status(HttpStatus.OK).body(orderStatus);
            }
            case "CANCELLED" -> {
                // Provide extra detail for a cancelled order
                String cancellationReason = order.getCancellationReason();
                return ResponseEntity.ok(String.format("Status: %s%nCancellation reason: %s", orderStatus, cancellationReason));
            }
            default -> {
                // Handle unknown or non-terminal statuses
                return ResponseEntity.notFound().build();
            }
        }
    }

}

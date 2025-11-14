package com.demo.service;

import com.demo.common.event.Event;
import com.demo.model.Address;
import com.demo.model.Order;
import com.demo.model.Status;
import com.demo.model.dto.CreateOrderRequest;
import com.demo.model.dto.OrderCreatedResponse;
import com.demo.repository.AddressRepository;
import com.demo.repository.OrderRepository;
import com.demo.utility.EventBuilder;
import com.demo.utility.ObjectBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final OutboxEventService outboxEventService;

    @Transactional
    public OrderCreatedResponse create(CreateOrderRequest request) {
        // 1. Persist the Address
        Address shippingAddress = ObjectBuilder.address(request.getShippingAddress());
        Address registeredShippingAddress = this.addressRepository.saveAndFlush(shippingAddress);

        // 2. Build and persist the Order (and its products)
        Order order = ObjectBuilder.order(request, registeredShippingAddress);
        Order registeredOrder = orderRepository.saveAndFlush(order);

        // 3. Build the DTO response
        OrderCreatedResponse orderCreatedResponse = ObjectBuilder.orderCreatedResponse(registeredOrder);

        // 4. Build the event for the saga
        Event orderCreatedEvent = EventBuilder.orderCreatedEvent(orderCreatedResponse);

        // 5. Save the event to the outbox (Transactional)
        this.outboxEventService.create(orderCreatedEvent);

        // 6. Return the DTO
        return orderCreatedResponse;
    }

    public Order retrieve(UUID orderId) {
        return this.orderRepository.findById(orderId).orElseThrow();
    }

    @Transactional
    public void updateStatus(UUID orderId, Status newStatus) {
        Order order = this.retrieve(orderId); // Uses the retrieve method
        order.setStatus(newStatus);
        this.orderRepository.saveAndFlush(order);
    }

    @Transactional
    public void setCancellationReason(UUID orderId, String reason) {
        Order order = this.retrieve(orderId); // Uses the retrieve method
        order.setCancellationReason(reason);
        this.orderRepository.saveAndFlush(order);
    }

}

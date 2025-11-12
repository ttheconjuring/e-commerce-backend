package com.demo.service.impl;

import com.demo.common.event.Event;
import com.demo.model.Address;
import com.demo.model.Order;
import com.demo.model.OrderProduct;
import com.demo.model.Status;
import com.demo.model.dto.CreateOrderRequest;
import com.demo.model.dto.OrderCreatedResponse;
import com.demo.repository.AddressRepository;
import com.demo.repository.OrderRepository;
import com.demo.service.OrderService;
import com.demo.service.OutboxEventService;
import com.demo.utility.EventBuilder;
import com.demo.utility.ObjectBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Concrete implementation of the {@link OrderService} interface.
 * <p>
 * This class contains the primary business logic for the Order Service,
 * including order creation and applying status updates from the saga.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final OutboxEventService outboxEventService;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation executes several steps within a single database transaction:</br>
     * 1. Saves the {@link Address} entity.</br>
     * 2. Builds and saves the {@link Order} entity (with its child {@link OrderProduct} list).</br>
     * 3. Builds the {@link OrderCreatedResponse} DTO to return.</br>
     * 4. Builds the {@link Event} payload.</br>
     * 5. Saves the event to the outbox via {@link OutboxEventService}.
     * <p>
     * Because this is {@link @Transactional}, it guarantees that the order
     * and its corresponding "OrderCreated" event are saved atomically.
     */
    @Transactional
    @Override
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

    /**
     * {@inheritDoc}
     *
     * @throws java.util.NoSuchElementException if not found.
     */
    @Override
    public Order retrieve(UUID orderId) {
        return this.orderRepository.findById(orderId).orElseThrow();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This operation is transactional. It finds the order, updates its
     * status field, and saves the change.
     */
    @Transactional
    @Override
    public void updateStatus(UUID orderId, Status newStatus) {
        Order order = this.retrieve(orderId); // Uses the retrieve method
        order.setStatus(newStatus);
        this.orderRepository.saveAndFlush(order);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This operation is transactional. It finds the order, updates its
     * cancellation reason, and saves the change.
     */
    @Transactional
    @Override
    public void setCancellationReason(UUID orderId, String reason) {
        Order order = this.retrieve(orderId); // Uses the retrieve method
        order.setCancellationReason(reason);
        this.orderRepository.saveAndFlush(order);
    }

}

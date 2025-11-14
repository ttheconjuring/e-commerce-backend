package com.demo.service;

import com.demo.common.event.order.OrderCreatedEvent;
import com.demo.common.payload.Payload;
import com.demo.common.payload.payment.*;
import com.demo.common.payload.product.ProductsShortagePayload;
import com.demo.common.payload.shipment.ArrangementFailedPayload;
import com.demo.model.OrderState;
import com.demo.model.OrderStateHistory;
import com.demo.model.Status;
import com.demo.repository.OrderStateHistoryRepository;
import com.demo.repository.OrderStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStateService {

    private final OrderStateRepository orderRepository;
    private final OrderStateHistoryRepository orderStateHistoryRepository;

    @Transactional
    public void create(OrderCreatedEvent orderCreatedEvent) {
        // 1. Create the OrderState entity
        OrderState orderState = new OrderState();
        orderState.setOrderId(orderCreatedEvent.getCorrelationId());
        orderState.setStatus(Status.CREATED);
        orderState.setOrderCreatedPayload(orderCreatedEvent.getPayload());
        orderState.setFailureReason(null);
        orderState.setCreatedAt(Instant.now());
        orderState.setUpdatedAt(Instant.now());
        // 2. Save the current state
        OrderState registeredOrderState = this.orderRepository.saveAndFlush(orderState);
        // 3. Save the history (audit log)
        this.orderStateHistoryRepository.saveAndFlush(new OrderStateHistory(registeredOrderState, registeredOrderState.getOrderCreatedPayload()));
    }

    public OrderState retrieve(UUID orderId) {
        return this.orderRepository.findById(orderId).orElseThrow();
    }

    @Transactional
    public void reflectPayment(UUID orderId, Payload paymentPayload) {
        // 1. Find the current state
        OrderState orderState = this.retrieve(orderId);
        // 2. Apply logic based on payload type
        if (paymentPayload instanceof PaymentSucceededPayload) {
            orderState.setPaymentSucceededPayload(paymentPayload);
            orderState.setStatus(Status.PAYMENT_SUCCEEDED);
        } else {
            PaymentFailedPayload paymentFailedPayload = (PaymentFailedPayload) paymentPayload;
            orderState.setPaymentFailedPayload(paymentFailedPayload);
            orderState.setStatus(Status.PAYMENT_FAILED);
            orderState.setFailureReason(paymentFailedPayload.getReason()); // Capture failure reason
        }
        orderState.setUpdatedAt(Instant.now());
        // 3. Save the updated state
        OrderState updatedOrderState = this.orderRepository.saveAndFlush(orderState);
        // 4. Save the history (audit log)
        this.orderStateHistoryRepository.saveAndFlush(new OrderStateHistory(updatedOrderState, paymentPayload));
    }

    @Transactional
    public OrderState reflectProductsUnavailability(UUID orderId, Payload productsShortagePayload) {
        // 1. Find the current state
        OrderState orderState = this.retrieve(orderId);
        ProductsShortagePayload payload = (ProductsShortagePayload) productsShortagePayload;
        orderState.setStatus(Status.PRODUCTS_UNAVAILABILITY);
        orderState.setFailureReason(payload.getReason()); // Capture failure reason
        orderState.setUpdatedAt(Instant.now());
        // 3. Save the updated state
        OrderState updatedOrderState = this.orderRepository.saveAndFlush(orderState);
        // 4. Save the history (audit log)
        this.orderStateHistoryRepository.saveAndFlush(new OrderStateHistory(updatedOrderState, payload));
        return updatedOrderState;
    }

    @Transactional
    public void reflectShipmentArrangement(UUID orderId, Payload shipmentArrangedPayload) {
        // 1. Find the current state
        OrderState orderState = this.retrieve(orderId);
        orderState.setShipmentArrangedPayload(shipmentArrangedPayload);
        orderState.setStatus(Status.SHIPMENT_ARRANGED);
        orderState.setUpdatedAt(Instant.now());
        // 3. Save the updated state
        OrderState updatedOrderState = this.orderRepository.saveAndFlush(orderState);
        // 4. Save the history (audit log)
        this.orderStateHistoryRepository.saveAndFlush(new OrderStateHistory(updatedOrderState, shipmentArrangedPayload));
    }

    @Transactional
    public OrderState reflectShipmentArrangementFailure(UUID orderId, Payload arrangementFailedPayload) {
        // 1. Find the current state
        OrderState orderState = this.retrieve(orderId);
        ArrangementFailedPayload payload = (ArrangementFailedPayload) arrangementFailedPayload;
        orderState.setArrangementFailedPayload(payload);
        orderState.setStatus(Status.SHIPMENT_ARRANGEMENT_FAILED);
        orderState.setFailureReason(payload.getReason());
        orderState.setUpdatedAt(Instant.now());
        // 3. Save the updated state
        OrderState updatedOrderState = this.orderRepository.saveAndFlush(orderState);
        // 4. Save the history (audit log)
        this.orderStateHistoryRepository.saveAndFlush(new OrderStateHistory(updatedOrderState, payload));
        return updatedOrderState;
    }

    @Transactional
    public OrderState updateStatus(UUID orderId, Status newStatus) {
        // 1. Find the current state
        OrderState orderState = this.retrieve(orderId);
        orderState.setStatus(newStatus);
        orderState.setUpdatedAt(Instant.now());
        // 3. Save the updated state
        OrderState updatedOrderState = this.orderRepository.saveAndFlush(orderState);
        // 4. Save the history (audit log)
        this.orderStateHistoryRepository.saveAndFlush(new OrderStateHistory(updatedOrderState, null));
        return updatedOrderState;
    }

}

package com.demo.service.impl;

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
import com.demo.service.OrderStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Concrete implementation of the {@link OrderStateService} interface.
 * <p>
 * This service is the "brain" of the saga orchestrator. It is responsible for
 * maintaining the *current state* of the saga in the {@link OrderState} entity.
 * <p>
 * A critical feature of this implementation is that **every state change
 * also creates an immutable {@link OrderStateHistory} record**. This is
 * accomplished by saving to both the {@link OrderStateRepository} and
 * {@link OrderStateHistoryRepository} within the same {@link @Transactional}
 * method, ensuring a complete and atomic audit log.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStateServiceImpl implements OrderStateService {

    private final OrderStateRepository orderRepository;
    private final OrderStateHistoryRepository orderStateHistoryRepository;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation creates the initial {@link OrderState} from the
     * {@link OrderCreatedEvent}, sets the status to {@link Status#CREATED},
     * and saves it. It also creates the *very first*
     * {@link OrderStateHistory} record for the saga.
     * Both saves are atomic due to the {@link @Transactional} boundary.
     */
    @Transactional
    @Override
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

    /**
     * {@inheritDoc}
     *
     * @throws java.util.NoSuchElementException if the orderId is not found.
     */
    @Override
    public OrderState retrieve(UUID orderId) {
        return this.orderRepository.findById(orderId).orElseThrow();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation inspects the {@code paymentPayload} using
     * {@code instanceof} to determine if the payment succeeded or failed.
     * <ul>
     * <li><b>On Success:</b> Sets {@link Status#PAYMENT_SUCCEEDED}.</li>
     * <li><b>On Failure:</b> Sets {@link Status#PAYMENT_FAILED} and
     * copies the {@code failureReason} from the payload.</li>
     * </ul>
     * It then saves the updated state and a new history record atomically.
     */
    @Transactional
    @Override
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

    /**
     * {@inheritDoc}
     * <p>
     * This implementation updates the saga state to
     * {@link Status#PRODUCTS_UNAVAILABILITY} and records the
     * {@code failureReason} from the payload.
     * It saves the updated state and a new history record atomically.
     */
    @Transactional
    @Override
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

    /**
     * {@inheritDoc}
     * <p>
     * This implementation records the {@code shipmentArrangedPayload} in
     * the {@link OrderState} and updates the status to
     * {@link Status#SHIPMENT_ARRANGED}.
     * It saves the updated state and a new history record atomically.
     */
    @Override
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

    /**
     * {@inheritDoc}
     * <p>
     * This implementation updates the saga state to
     * {@link Status#SHIPMENT_ARRANGEMENT_FAILED} and records the
     * {@code failureReason} from the payload.
     * It saves the updated state and a new history record atomically.
     */
    @Transactional
    @Override
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

    /**
     * {@inheritDoc}
     * <p>
     * This is a general-purpose method used by the event handlers to
     * advance the saga's internal status (e.g., to `PENDING_PAYMENT`).
     * It updates the status, saves the state, and creates a history
     * record with a {@code null} payload, as this is an internal
     * state transition, not one triggered by an external event.
     */
    @Transactional
    @Override
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

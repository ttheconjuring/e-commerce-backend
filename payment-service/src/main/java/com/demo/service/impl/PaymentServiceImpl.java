package com.demo.service.impl;

import com.demo.common.payload.Payload;
import com.demo.common.payload.payment.ProcessPaymentPayload;
import com.demo.model.Payment;
import com.demo.model.Status;
import com.demo.repository.PaymentRepository;
import com.demo.service.PaymentService;
import com.demo.utility.Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Concrete implementation of the {@link PaymentService} interface.
 * <p>
 * This class handles the logic of building and persisting {@link Payment}
 * entities to the database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation builds a new {@link Payment} entity from the
     * command payload and the given status. It uses a {@link Generator}
     * utility to simulate the creation of a transaction ID and a failure
     * reason (if applicable).
     * <p>
     * This method is {@link @Transactional} and flushes the entity to the
     * database immediately.
     */
    @Transactional
    @Override
    public Payment save(Payload processPaymentPayload, Status status) {
        // Cast the generic payload to the specific type
        ProcessPaymentPayload payload = (ProcessPaymentPayload) processPaymentPayload;

        // Build the new Payment entity
        Payment payment = new Payment();
        payment.setOrderId(payload.getOrderId());
        payment.setAmount(payload.getTotalAmount());
        payment.setCurrency(payload.getCurrency());
        payment.setStatus(status);

        // --- Simulated Payment Gateway Logic ---
        // Conditionally set failure reason or transaction ID based on status
        payment.setFailureReason(status == Status.PAYMENT_SUCCEEDED ? null : Generator.failureReason());
        payment.setPaymentMethod(payment.getPaymentMethod());
        payment.setTransactionId(Generator.transactionId());
        // --- End of Simulation ---

        // Set timestamps
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());

        // Save and return the persisted entity
        return this.paymentRepository.saveAndFlush(payment);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates directly to the {@link PaymentRepository}.
     */
    @Override
    public Payment retrieve(String transactionId) {
        return this.paymentRepository.findByTransactionId(transactionId);
    }

}

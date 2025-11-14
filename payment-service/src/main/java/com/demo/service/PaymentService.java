package com.demo.service;

import com.demo.common.payload.Payload;
import com.demo.common.payload.payment.ProcessPaymentPayload;
import com.demo.model.Payment;
import com.demo.model.Status;
import com.demo.repository.PaymentRepository;
import com.demo.utility.Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
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

}

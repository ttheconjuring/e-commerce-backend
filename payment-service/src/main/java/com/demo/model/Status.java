package com.demo.model;

public enum Status {
    // Payment
    PAYMENT_SUCCEEDED,
    REFUND_SUCCEEDED,
    PAYMENT_FAILED,

    // Outbox
    PENDING_PUBLISHING, PUBLISHED, PUBLISHING_FAILED
}

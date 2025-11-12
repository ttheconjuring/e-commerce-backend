package com.demo.model;

public enum Status {
    // Order
    PLACED, COMPLETED, CANCELLED,

    // Outbox
    PENDING_PUBLISHING, PUBLISHED, PUBLISHING_FAILED
}

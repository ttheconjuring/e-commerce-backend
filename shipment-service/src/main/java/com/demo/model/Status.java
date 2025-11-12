package com.demo.model;

public enum Status {
    // Shipment
    ARRANGED, FAILED, CANCELLED,

    // Outbox
    PENDING_PUBLISHING, PUBLISHED, PUBLISHING_FAILED
}

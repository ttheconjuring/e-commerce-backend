package com.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for a {@link com.demo.model.DltMessage}.
 * <p>
 * This class represents a simplified, read-only view of a failed message,
 * intended for display to an administrator via the
 * {@link com.demo.controller.ErrorController}. It only includes the
 * essential information needed for a summary view.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DltMessageDTO {

    /**
     * The unique primary key of the DLT record in the database.
     */
    private UUID id;

    /**
     * The original unique ID of the failed message.
     */
    private UUID messageId;

    /**
     * A human-readable description of the failure
     * (e.g., "PAYMENT-SERVICE couldn't process PROCESS_PAYMENT.").
     */
    private String description;

    /**
     * The timestamp of when the DLT service first received this failed message.
     */
    private Instant receivedAt;

}

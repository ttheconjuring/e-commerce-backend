package com.demo.controller;

import com.demo.model.DltMessageDTO;
import com.demo.service.DltMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API Controller for the Dead-Letter Topic (DLT) Service.
 * <p>
 * This controller provides administrative endpoints for viewing and managing
 * "dead-letter" messages. These are messages that have failed all
 * processing attempts (including retries) in other microservices
 * (e.g., order-service, payment-service) and have been routed to a
 * Dead Letter Topic for manual inspection.
 *
 * @see com.demo.service.DltMessageService
 */
@RestController
@RequestMapping("/error")
@RequiredArgsConstructor
public class ErrorController {

    private final DltMessageService dltMessageService;

    /**
     * Retrieves a list of all captured dead-letter messages.
     * <p>
     * This endpoint is intended for administrators to review all messages
     * that have failed processing across the microservice ecosystem.
     * It fetches all records that this DLT service has consumed
     * and saved.
     *
     * @return A {@link ResponseEntity} with status 200 (OK) and a list
     * of {@link DltMessageDTO} objects in the body, representing
     * the failed messages.
     */
    @GetMapping("/check")
    public ResponseEntity<List<DltMessageDTO>> listErrors() {
        return ResponseEntity.ok(this.dltMessageService.retrieveAll());
    }

}

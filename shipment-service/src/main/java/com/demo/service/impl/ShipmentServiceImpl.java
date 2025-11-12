package com.demo.service.impl;

import com.demo.common.payload.Payload;
import com.demo.common.payload.shipment.ArrangeShipmentPayload;
import com.demo.model.Carrier;
import com.demo.model.Shipment;
import com.demo.model.Status;
import com.demo.repository.ShipmentRepository;
import com.demo.service.ShipmentService;
import com.demo.utility.Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation builds a new {@link Shipment} entity from the command
     * payload and the given status. It denormalizes the shipping address
     * data from the payload into the new {@link Shipment} record.
     * <p>
     * <b>Note:</b> This implementation uses a {@link Generator} to simulate
     * carrier responses (like failure reasons and tracking numbers).
     * Several fields (carrier, recipientName) are currently hardcoded.
     */
    @Transactional
    @Override
    public Shipment create(Payload arrangeShipmentPayload, Status status) {
        // Cast the generic payload to the specific type
        ArrangeShipmentPayload payload = (ArrangeShipmentPayload) arrangeShipmentPayload;

        // Build the new Shipment entity
        Shipment shipment = new Shipment();
        shipment.setOrderId(payload.getOrderId());
        shipment.setStatus(status);
        shipment.setFailureReason(status == Status.ARRANGED ? null : Generator.failureReason());
        shipment.setCancellationReason(null); // Not cancelled at creation

        // --- Simulated Carrier API Logic ---
        shipment.setCarrier(Carrier.UPS); // TODO: hardcoded
        shipment.setTrackingNumber(Generator.trackingNumber());
        // --- End of Simulation ---

        // Denormalize address data
        shipment.setRecipientName("null"); // TODO: obtain the user name or use the customer id instead
        shipment.setAddress(payload.getShippingAddress().getAddress());
        shipment.setCity(payload.getShippingAddress().getCity());
        shipment.setPostalCode(payload.getShippingAddress().getPostalCode());
        shipment.setCountry(payload.getShippingAddress().getCountry());

        // Set timestamps
        shipment.setCreatedAt(Instant.now());
        shipment.setUpdatedAt(Instant.now());

        // Save and return
        return this.shipmentRepository.saveAndFlush(shipment);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation finds the {@link Shipment} by its ID, updates its
     * status to {@link Status#CANCELLED}, records the cancellation reason,
     * and updates the timestamp.
     *
     * @throws java.util.NoSuchElementException if the shipmentId is not found.
     */
    @Transactional
    @Override
    public Shipment cancel(UUID shipmentId, String reason) {
        // Find the shipment or throw an exception
        Shipment shipment = this.shipmentRepository.findById(shipmentId).orElseThrow();
        // Apply the cancellation
        shipment.setStatus(Status.CANCELLED);
        shipment.setCancellationReason(reason);
        shipment.setUpdatedAt(Instant.now());
        // Save and return
        return this.shipmentRepository.saveAndFlush(shipment);
    }

}

package com.demo.component.impl;

import com.demo.common.command.shipment.ArrangeShipmentCommand;
import com.demo.common.command.shipment.CancelShipmentCommand;
import com.demo.common.constant.Topics;
import com.demo.common.event.shipment.ArrangementFailedEvent;
import com.demo.common.event.shipment.ShipmentArrangedEvent;
import com.demo.common.event.shipment.ShipmentCancelledEvent;
import com.demo.component.ShipmentDltHandler;
import com.demo.service.DltMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete implementation of the {@link ShipmentDltHandler} interface.
 * <p>
 * This class is a "multi-method" Kafka listener that consumes from
 * both the {@link Topics#SHIPMENT_EVENTS_TOPIC_DLT} and
 * {@link Topics#SHIPMENT_COMMANDS_TOPIC_DLT}.
 * <p>
 * The {@link KafkaHandler} annotation routes the deserialized message
 * to the correct method based on its class type (e.g., a
 * {@code ShipmentArrangedEvent} message goes to the
 * {@code handleShipmentArrangedEvent} method).
 * <p>
 * The sole responsibility of each handler is to delegate the failed
 * message to the {@link DltMessageService} to be saved to the database.
 */
@Component
@RequiredArgsConstructor
@KafkaListener(topics = {Topics.SHIPMENT_EVENTS_TOPIC_DLT, Topics.SHIPMENT_COMMANDS_TOPIC_DLT})
public class ShipmentDltHandlerImpl implements ShipmentDltHandler {

    private final DltMessageService dltMessageService;

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleArrangementFailedEvent(ArrangementFailedEvent arrangementFailedEvent) {
        this.dltMessageService.register(arrangementFailedEvent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleShipmentArrangedEvent(ShipmentArrangedEvent shipmentArrangedEvent) {
        this.dltMessageService.register(shipmentArrangedEvent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleShipmentCancelledEvent(ShipmentCancelledEvent shipmentCancelledEvent) {
        this.dltMessageService.register(shipmentCancelledEvent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleArrangeShipmentCommand(ArrangeShipmentCommand arrangeShipmentCommand) {
        this.dltMessageService.register(arrangeShipmentCommand);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleCancelShipmentCommand(CancelShipmentCommand cancelShipmentCommand) {
        this.dltMessageService.register(cancelShipmentCommand);
    }

}

package com.demo.component;

import com.demo.common.command.shipment.ArrangeShipmentCommand;
import com.demo.common.command.shipment.CancelShipmentCommand;
import com.demo.common.constant.Topics;
import com.demo.common.event.shipment.ArrangementFailedEvent;
import com.demo.common.event.shipment.ShipmentArrangedEvent;
import com.demo.common.event.shipment.ShipmentCancelledEvent;
import com.demo.service.DltMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = {Topics.SHIPMENT_EVENTS_TOPIC_DLT, Topics.SHIPMENT_COMMANDS_TOPIC_DLT})
public class ShipmentDltHandler {

    private final DltMessageService dltMessageService;

    @Transactional
    @KafkaHandler
    public void handleArrangementFailedEvent(ArrangementFailedEvent arrangementFailedEvent) {
        this.dltMessageService.register(arrangementFailedEvent);
    }

    @Transactional
    @KafkaHandler
    public void handleShipmentArrangedEvent(ShipmentArrangedEvent shipmentArrangedEvent) {
        this.dltMessageService.register(shipmentArrangedEvent);
    }

    @Transactional
    @KafkaHandler
    public void handleShipmentCancelledEvent(ShipmentCancelledEvent shipmentCancelledEvent) {
        this.dltMessageService.register(shipmentCancelledEvent);
    }

    @Transactional
    @KafkaHandler
    public void handleArrangeShipmentCommand(ArrangeShipmentCommand arrangeShipmentCommand) {
        this.dltMessageService.register(arrangeShipmentCommand);
    }

    @Transactional
    @KafkaHandler
    public void handleCancelShipmentCommand(CancelShipmentCommand cancelShipmentCommand) {
        this.dltMessageService.register(cancelShipmentCommand);
    }

}

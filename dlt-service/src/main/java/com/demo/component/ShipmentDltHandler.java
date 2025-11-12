package com.demo.component;

import com.demo.common.command.shipment.ArrangeShipmentCommand;
import com.demo.common.command.shipment.CancelShipmentCommand;
import com.demo.common.event.shipment.ArrangementFailedEvent;
import com.demo.common.event.shipment.ShipmentArrangedEvent;
import com.demo.common.event.shipment.ShipmentCancelledEvent;

/**
 * Defines the contract for a consumer that listens to the Dead-Letter Topics
 * (DLTs) related to the Shipment service.
 * <p>
 * This interface provides strongly-typed methods for each type of failed
 * shipment-related message. It allows the consumer to catch, deserialize,
 * and route any failed message to the {@link com.demo.service.DltMessageService}
 * for registration and later inspection.
 *
 * @see com.demo.component.impl.ShipmentDltHandlerImpl
 * @see com.demo.service.DltMessageService
 */
public interface ShipmentDltHandler {

    // Event

    /**
     * Processes a failed {@link ArrangementFailedEvent} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>order-saga-orchestrator</b>.
     *
     * @param arrangementFailedEvent The failed message.
     */
    void handleArrangementFailedEvent(ArrangementFailedEvent arrangementFailedEvent);

    /**
     * Processes a failed {@link ShipmentArrangedEvent} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>order-saga-orchestrator</b>.
     *
     * @param shipmentArrangedEvent The failed message.
     */
    void handleShipmentArrangedEvent(ShipmentArrangedEvent shipmentArrangedEvent);

    /**
     * Processes a failed {@link ShipmentCancelledEvent} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>order-saga-orchestrator</b>.
     *
     * @param shipmentCancelledEvent The failed message.
     */
    void handleShipmentCancelledEvent(ShipmentCancelledEvent shipmentCancelledEvent);

    // Commands

    /**
     * Processes a failed {@link ArrangeShipmentCommand} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>shipment-service</b>.
     *
     * @param arrangeShipmentCommand The failed message.
     */
    void handleArrangeShipmentCommand(ArrangeShipmentCommand arrangeShipmentCommand);

    /**
     * Processes a failed {@link CancelShipmentCommand} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>shipment-service</b>.
     *
     * @param cancelShipmentCommand The failed message.
     */
    void handleCancelShipmentCommand(CancelShipmentCommand cancelShipmentCommand);

}

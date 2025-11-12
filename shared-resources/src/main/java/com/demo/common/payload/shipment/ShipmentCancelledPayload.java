package com.demo.common.payload.shipment;

import com.demo.common.payload.Payload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentCancelledPayload implements Payload {

    private UUID orderId;

    private UUID shipmentId;

}

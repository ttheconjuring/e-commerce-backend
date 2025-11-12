package com.demo.common.payload.shipment;

import com.demo.common.payload.Payload;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentArrangedPayload implements Payload {

    private UUID orderId;

    private UUID shipmentId;

    private String trackingNumber;

    private String carrier;

}

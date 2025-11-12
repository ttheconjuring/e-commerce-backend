package com.demo.common.payload.shipment;

import com.demo.common.payload.Payload;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArrangementFailedPayload implements Payload {

    private UUID orderId;

    private String reason;

    private Instant failedAt;

}

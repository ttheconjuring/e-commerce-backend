package com.demo.common.payload.payment;

import com.demo.common.payload.Payload;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedPayload implements Payload {

    private UUID orderId;

    private String reason;

    private Instant processedAt;

}

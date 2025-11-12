package com.demo.common.payload.payment;

import com.demo.common.payload.Payload;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSucceededPayload implements Payload {

    private UUID orderId;

    private String transactionId;

}

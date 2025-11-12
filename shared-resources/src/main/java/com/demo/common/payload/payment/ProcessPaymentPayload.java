package com.demo.common.payload.payment;

import com.demo.common.payload.Payload;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentPayload implements Payload {

    private UUID orderId;

    private BigDecimal totalAmount;

    private String currency;

    private String paymentMethodId;

}

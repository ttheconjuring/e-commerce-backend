package com.demo.common.payload.order;

import com.demo.common.payload.Payload;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledPayload implements Payload {

    private UUID orderId;

    private String finalStatus;

    private String reason;

}

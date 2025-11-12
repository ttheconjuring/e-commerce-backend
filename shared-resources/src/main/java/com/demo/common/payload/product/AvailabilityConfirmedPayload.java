package com.demo.common.payload.product;

import com.demo.common.payload.Payload;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityConfirmedPayload implements Payload {

    private UUID orderId;

}

package com.demo.common.payload.order;

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
public class OrderCompletedPayload implements Payload {

    private UUID orderId;

    private String finalStatus;

}

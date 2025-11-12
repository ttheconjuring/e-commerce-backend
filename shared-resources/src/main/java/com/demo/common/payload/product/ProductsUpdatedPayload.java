package com.demo.common.payload.product;

import com.demo.common.payload.Payload;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductsUpdatedPayload implements Payload {

    private UUID orderId;

}

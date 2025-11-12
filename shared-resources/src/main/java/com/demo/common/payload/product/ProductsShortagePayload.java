package com.demo.common.payload.product;

import com.demo.common.dto.InsufficientProductDTO;
import com.demo.common.payload.Payload;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductsShortagePayload implements Payload {

    private UUID orderId;

    private String reason;

    List<InsufficientProductDTO> outOfStockProducts;

}

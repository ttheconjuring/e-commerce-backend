package com.demo.common.payload.product;

import com.demo.common.dto.ProductQuantityDTO;
import com.demo.common.payload.Payload;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductsPayload implements Payload {

    private UUID orderId;

    private List<ProductQuantityDTO> productsToDecrement;

}

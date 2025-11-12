package com.demo.common.payload.product;

import com.demo.common.dto.ProductQuantityDTO;
import com.demo.common.payload.Payload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmAvailabilityPayload implements Payload {

    private UUID orderId;

    private List<ProductQuantityDTO> productsToCheck;

}

package com.demo.common.payload.shipment;

import com.demo.common.dto.AddressDTO;
import com.demo.common.dto.ProductQuantityDTO;
import com.demo.common.payload.Payload;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArrangeShipmentPayload implements Payload {

    private UUID orderId;

    private UUID customerId;

    private List<ProductQuantityDTO> products;

    private AddressDTO shippingAddress;

}

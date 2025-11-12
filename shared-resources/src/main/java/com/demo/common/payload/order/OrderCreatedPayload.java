package com.demo.common.payload.order;

import com.demo.common.dto.AddressDTO;
import com.demo.common.dto.OrderProductDTO;
import com.demo.common.payload.Payload;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedPayload implements Payload {

    private UUID orderId;

    private UUID customerId;

    private List<OrderProductDTO> products;

    private AddressDTO shippingAddress;

    private BigDecimal totalAmount;

    private String currency;

    private String paymentMethodId;

    private String carrier;

}




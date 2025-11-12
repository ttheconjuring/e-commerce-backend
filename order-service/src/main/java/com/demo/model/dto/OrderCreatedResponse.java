package com.demo.model.dto;

import com.demo.common.dto.AddressDTO;
import com.demo.common.dto.OrderProductDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedResponse {

    private UUID orderId;

    private String customerId;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;

    private List<OrderProductDTO> products;

    private AddressDTO shippingAddress;

    private BigDecimal totalAmount;

    private String currency;

    private String paymentMethodId;

    private String carrier;

}

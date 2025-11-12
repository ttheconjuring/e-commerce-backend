package com.demo.model.dto;

import com.demo.common.dto.AddressDTO;
import com.demo.common.dto.OrderProductDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "Customer ID is required.")
    private String customerId;

    @NotEmpty(message = "Order must contain at least one item.")
    @Valid // Cascades validation to each item in the list
    private List<OrderProductDTO> products;

    @NotNull(message = "Shipping address is required.")
    @Valid // This annotation cascades validation to the AddressRequest object
    private AddressDTO shippingAddress;

    @NotNull(message = "Total amount is required.")
    @PositiveOrZero(message = "Total amount must be zero or positive.")
    private BigDecimal totalAmount;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters.")
    private String currency;

    @NotBlank(message = "A payment method must be chosen.")
    private String paymentMethodId;

    @NotBlank(message = "A carrier must be selected.")
    private String carrier;

}
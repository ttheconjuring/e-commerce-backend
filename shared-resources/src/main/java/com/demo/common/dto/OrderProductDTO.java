package com.demo.common.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderProductDTO {

    private UUID productId;

    private Integer quantity;

    private BigDecimal pricePerUnit;

}

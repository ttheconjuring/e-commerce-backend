package com.demo.common.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsufficientProductDTO {

    private UUID productId;

    private Integer requestedQuantity;

    private Integer availableQuantity;

}

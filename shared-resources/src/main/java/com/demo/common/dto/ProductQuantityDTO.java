package com.demo.common.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductQuantityDTO {

    private UUID productId;

    private Integer quantity;

}

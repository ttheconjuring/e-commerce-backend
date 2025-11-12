package com.demo.common.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AddressDTO {

    private String address;

    private String postalCode;

    private String city;

    private String country;

}

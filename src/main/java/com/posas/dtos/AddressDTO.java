package com.posas.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDTO {
    String city;
    String country;
    String line1;
    String line2;
    String postalCode;
    String state;
}

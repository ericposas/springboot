package com.posas.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDTO {
    Integer streetnum;
    String streetname;
    String city;
    String state;
    String postalCode;
}

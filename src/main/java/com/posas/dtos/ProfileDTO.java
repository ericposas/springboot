package com.posas.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileDTO {
    String firstname;
    String lastname;
    String email;
    AddressDTO address;
}

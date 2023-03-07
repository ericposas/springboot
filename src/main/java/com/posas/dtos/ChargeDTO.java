package com.posas.dtos;

import lombok.Data;

@Data
public class ChargeDTO {
    Integer amount;
    String paymentMethodId;
}

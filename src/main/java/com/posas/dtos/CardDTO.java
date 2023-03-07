package com.posas.dtos;

import lombok.Data;

@Data
public class CardDTO {
    String cardNumber;
    Integer expMonth;
    Integer expYear;
    String cvc;
}

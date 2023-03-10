package com.posas.dtos;

import lombok.Data;

@Data
public class CardDTO {
    private String cardNumber;
    private Integer expMonth;
    private Integer expYear;
    private String cvc;
}

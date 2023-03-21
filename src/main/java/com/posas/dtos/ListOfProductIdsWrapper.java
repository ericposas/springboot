package com.posas.dtos;

import java.util.List;

import lombok.Data;

@Data
public class ListOfProductIdsWrapper {
    List<Long> productIds;
}

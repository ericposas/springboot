package com.posas.dtos;

import java.util.List;

import com.posas.entities.Product;

import lombok.Data;

@Data
public class LineItemsBodyDTO {
    List<Product> products;
}
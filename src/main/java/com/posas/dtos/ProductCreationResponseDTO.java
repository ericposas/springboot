package com.posas.dtos;

import com.posas.entities.Product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductCreationResponseDTO {
    private Product storeProduct;
    private String stripeProduct;
}

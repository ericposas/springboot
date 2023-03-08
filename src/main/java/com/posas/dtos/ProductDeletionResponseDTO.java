package com.posas.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDeletionResponseDTO {
    String stripeProduct;
    com.posas.entities.Product storeProduct;
    String message;
}

package com.posas.dtos;

import lombok.Data;

@Data
public class ProductDTO {
    private String productName;
    private String productDescription;
    private Long productPrice;
    private String providedImageUrl;
    private UnsplashSearchParams unsplashSearchParams;
}
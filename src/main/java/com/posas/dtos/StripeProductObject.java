package com.posas.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
class StripeProductMetadata {
    private String productId;
    private String name;
    private String description;
    private String price;
    private String imageUrl;
    private String pageUrl;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StripeProductObject {
    private String id;
    private String name;
    private String description;
    private List<String> images;
    private String default_price;
    private StripeProductMetadata metadata;
}
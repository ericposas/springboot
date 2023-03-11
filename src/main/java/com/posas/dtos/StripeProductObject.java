package com.posas.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
class StripeProductMetadata {
    private String db_product_id;
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
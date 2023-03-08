package com.posas.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "PRODUCTS", uniqueConstraints = @UniqueConstraint(columnNames = { "name" }))
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long product_id;
    String name;
    String description;
    String stripeProductId;
    Long price;
    String imageUrl;
    String pageUrl;
    Boolean deleted = false;
}

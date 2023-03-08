package com.posas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.posas.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("FROM Product product WHERE product.name = :productName")
    public Product findByName(@Param("productName") String productName);
}

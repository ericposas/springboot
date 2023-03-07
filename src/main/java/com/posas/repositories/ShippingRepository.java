package com.posas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.posas.entities.Shipping;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Long> {
}

package com.posas.entities;

import java.sql.Time;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "SHIPPING_ADDRESSES", uniqueConstraints = @UniqueConstraint(columnNames = { "profile_id" }))
public class Shipping {
    @Id
    @Column(name = "address_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long addressId;
    private String city;
    private String country;
    private String line1;
    private String line2;
    private String postalCode;
    private String state;
    private String type;
    private Time createdAt;
    private Time updatedAt;
    private Time deletedAt;

    @OneToOne
    @JsonBackReference
    @JoinColumn(name = "profile_id")
    private Profile profile;
}

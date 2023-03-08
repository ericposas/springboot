package com.posas.entities;

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

@Data
@Entity
@NoArgsConstructor
@Table(name = "BILLING_ADDRESSES", uniqueConstraints = @UniqueConstraint(columnNames = { "profile_id" }))
public class Address {
    @Id
    @Column(name = "address_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long addressId;
    String city;
    String country;
    String line1;
    String line2;
    String postalCode;
    String state;
    String type;

    @OneToOne
    @JsonBackReference
    @JoinColumn(name = "profile_id")
    Profile profile;
}

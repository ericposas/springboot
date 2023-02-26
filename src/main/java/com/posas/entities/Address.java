package com.posas.entities;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Builder
@Table(name = "ADDRESSES")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID address_id;
    Integer streetnum;
    String streetname;
    String city;
    String state;
    String postalCode;

    @OneToOne
    @JoinColumn(name = "profile_id")
    Profile profile;
}

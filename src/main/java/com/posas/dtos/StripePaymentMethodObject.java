package com.posas.dtos;

import java.util.List;

import lombok.Data;

@Data
class Networks {
    List<String> available;
    Object preferred;
}

@Data
class Address {
    String city;
    String country;
    String line1;
    String line2;
    String postal_code;
    String state;
}

@Data
class BillingDetails {
    Address address;
}

@Data
class Checks {
    String address_line1_check;
    String address_postal_code_check;
    String cvc_check;
}

@Data
class _3DUsage {
    Boolean supported;
}

@Data
class Card {
    String brand;
    Checks checks;
    String country;
    Object description;
    Long exp_month;
    Long exp_year;
    String fingerprint;
    String funding;
    Object iin;
    Object issuer;
    String last4;
    Networks networks;
    _3DUsage three_d_secure_usage;
    Object wallet;
}

@Data
public class StripePaymentMethodObject {
    Object acss_debit;
    Object affirm;
    Object afterpay_clearpay;
    Object alipay;
    Object au_becs_debit;
    Object bacs_debit;
    Object bancontact;
    BillingDetails billing_details;
    String email;
    String name;
    String phone;
    Object blik;
    Object boleto;
    Card card;
    Object card_present;
    Object cashapp;
    Long created;
    String customer;
    Object customer_balance;
    Object eps;
    Object fpx;
    Object giropay;
    Object grabpay;
    String id;
    Object ideal;
    Object interac_present;
    Object klarna;
    Object konbini;
    Object link;
    Boolean livemode;
    Object metadata;
    String object;
    Object oxxo;
    Object p24;
    Object paynow;
    Object paypal;
    Object pix;
    Object promptpay;
    Object radar_options;
    Object sepa_debit;
    Object sofort;
    String type;
    Object us_bank_account;
    Object wechat_pay;
    String zip;
}

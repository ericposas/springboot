package com.posas.helpers;

public class BaseURL {

    public static String getBaseUrl(String activeProfile, String append) {
        return (activeProfile.trim().equals("dev")
                ? "http://localhost"
                : "https://webcommerce.live") + append;
    }

}

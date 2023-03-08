package com.posas.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class HttpHelpers {

    public static HttpClient getHttpClient() {
        var client = HttpClient.newHttpClient();
        return client;
    }

    public static HttpRequest httpGET(String url) {
        var httpRequest = HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .header("accept", "application/json")
                .GET()
                .build();
        return httpRequest;
    }

}

package com.flenski.service;

import java.net.http.HttpClient;
import org.springframework.stereotype.Service;
import com.flenski.request.RequestInterface;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;


@Service
public class HttpService {

    private final HttpClient httpClient;

    public HttpService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpResponse<String> sendRequest(RequestInterface request) {
        try {
            HttpRequest httpRequest = request.build();
            HttpResponse<String> response = httpClient.send(httpRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
            System.out.println("Response status: " + response.statusCode());
            System.out.println("Response body: " + response.body());
            return response;
        } catch (Exception e) {
            System.err.println("Error sending HTTP request: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    
}

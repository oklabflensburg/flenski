package com.flenski.dto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;

public class IndexRequest {

    ArrayList<Point> points;

    public void addPoint(Point point) {
        if (points == null) {
            points = new ArrayList<>();
        }
        points.add(point);
    }

    public String toJson() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        java.util.HashMap<String, Object> result = new java.util.HashMap<>();
        java.util.List<Object> pointsList = new java.util.ArrayList<>();
        if (points != null) {
            for (Point p : points) {
                pointsList.add(p.toJsonMap());
            }
        }
        result.put("points", pointsList);
        try {
            return mapper.writeValueAsString(result);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to convert indexRequest to JSON", e);
        }
    }

    public HttpRequest build() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
        //TODO move this to config
                .uri(URI.create("http://localhost:6333/collections/hybrid/points/"))
                .PUT(HttpRequest.BodyPublishers.ofString(toJson()))
                .build();

        return request;
    }
}

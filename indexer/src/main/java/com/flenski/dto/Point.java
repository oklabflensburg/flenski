package com.flenski.dto;
import java.util.List;
import java.util.Map;


public class Point {
    private String id;
    private List<Vector> vectors = new java.util.ArrayList<>();
    private Map<String, Object> payload;

    public Map<String, Object> toJsonMap() {
        var point = new java.util.HashMap<String, Object>();
        point.put("id", id);
        var vectorObj = new java.util.HashMap<String, Object>();
        for (Vector v : vectors) {
            if (v.getName() != null) {
                vectorObj.put(v.getName(), v.toQdrantFormat());
            }
        }
        point.put("vector", vectorObj);
        point.put("payload", payload);
        return point;
    }

    public Point build(DocumentDto document, String content, Vector sparseVector, Vector denseVector) {
        
        this.id = java.util.UUID.nameUUIDFromBytes(content.getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
        this.addToPayload("sourceUrl", document.getSourceUrl());
        this.addToPayload("content", content);
        this.addToPayload("source_identifier", document.getSourceIdentifier());
        this.addToPayload("discovery_date_time", document.getDiscoveryDateTime() != null ? document.getDiscoveryDateTime().toString() : null);
        this.addToPayload("source_date_time", document.getSourceDateTime() != null ? document.getSourceDateTime().toString() : null);
        this.addVector(sparseVector);
        this.addVector(denseVector);

        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public java.util.List<Vector> getVectors() {
        return vectors;
    }

    public void setVectors(java.util.List<Vector> vectors) {
        this.vectors = vectors;
    }

    public void addVector(Vector vector) {
        this.vectors.add(vector);
    }

    public void addToPayload(String key, Object value) {
        if (this.payload == null) {
            this.payload = new java.util.HashMap<>();
        }
        this.payload.put(key, value);
    }
}

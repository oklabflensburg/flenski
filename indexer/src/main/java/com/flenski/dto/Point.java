package com.flenski.dto;

public class Point {
    private int id;
    private java.util.List<Vector> vectors = new java.util.ArrayList<>();

    public java.util.Map<String, Object> toJsonMap() {
        var point = new java.util.HashMap<String, Object>();
        point.put("id", id);
        var vectorObj = new java.util.HashMap<String, Object>();
        for (Vector v : vectors) {
            if (v.getName() != null) {
                vectorObj.put(v.getName(), v.toQdrantFormat());
            }
        }
        point.put("vector", vectorObj);
        return point;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
}

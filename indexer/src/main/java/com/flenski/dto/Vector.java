package com.flenski.dto;

public class Vector {

    private String name;
    private int[] indices;
    private float[] values;

    public String getName() {
        return name;
    }

    public Object toQdrantFormat() {
        if ("dense".equals(name)) {
            return values;
        } else if ("sparse".equals(name)) {
            var sparseObj = new java.util.HashMap<String, Object>();
            sparseObj.put("indices", indices);
            sparseObj.put("values", values);
            return sparseObj;
        } else {
            return values;
        }
    }
    public Vector(float[] values, int[] indices, String name) {
        this.indices = indices;
        this.values = values;
        this.name = name;
    }

    public Vector(float[] values, int[] indices) {
        this.indices = indices;
        this.values = values;
    }

    public Vector(float[] values) {
        this.values = values;
    }

    public Vector(float[] values, String name) {
        this.values = values;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getIndices() {
        return indices;
    }

    public void setIndices(int[] indices) {
        this.indices = indices;
    }

    public float[] getValues() {
        return values;
    }

    public void setValues(float[] values) {
        this.values = values;
    }
}

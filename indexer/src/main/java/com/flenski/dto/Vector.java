package com.flenski.dto;

public class Vector {

    private String name;
    private int[] indices;
    private double[] values;
    private VectorType type;

    public String getName() {
        return name;
    }

    public Object toQdrantFormat() {
        if (type == VectorType.SPARSE) {
            var sparseObj = new java.util.HashMap<String, Object>();
            sparseObj.put("indices", indices);
            sparseObj.put("values", values);
            return sparseObj;
        }
        return values;
    }

    public Vector(double[] values, int[] indices, String name) {
        this.type = VectorType.SPARSE;
        this.indices = indices;
        this.values = values;
        this.name = name;
    }

    public Vector(double[] values, int[] indices) {
        this.type = VectorType.SPARSE;
        this.indices = indices;
        this.values = values;
    }

    public Vector(double[] values) {
        this.type = VectorType.DENSE;
        this.values = values;
    }

    public Vector(double[] values, String name) {
        this.type = VectorType.DENSE;
        this.values = values;
        this.name = name;
    }

    public Vector(float[] values) {
        this.type = VectorType.DENSE;
        this.values = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            this.values[i] = values[i];
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getIndices() {
        return indices;
    }

    public double[] getValues() {
        return values;
    }

    public VectorType getType() {
        return type;
    }
}

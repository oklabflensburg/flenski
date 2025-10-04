package com.flenski.dto;

public class IndexResult {
    private int total;
    private int indexed;
    private int duplicates;
    private int failed;

    public IndexResult(int total, int indexed, int duplicates, int failed) {
        this.total = total;
        this.indexed = indexed;
        this.duplicates = duplicates;
        this.failed = failed;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getIndexed() {
        return indexed;
    }

    public void setIndexed(int indexed) {
        this.indexed = indexed;
    }

    public int getDuplicates() {
        return duplicates;
    }

    public void setDuplicates(int duplicates) {
        this.duplicates = duplicates;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }
}

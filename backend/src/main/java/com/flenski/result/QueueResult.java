package com.flenski.result;

public class QueueResult {
    private int added;
    private int duplicates;

    public QueueResult(int added, int duplicates) {
        this.added = added;
        this.duplicates = duplicates;
    }

    public int getAdded() {
        return added;
    }

    public void setAdded(int added) {
        this.added = added;
    }

    public int getDuplicates() {
        return duplicates;
    }

    public void setDuplicates(int duplicates) {
        this.duplicates = duplicates;
    }
}

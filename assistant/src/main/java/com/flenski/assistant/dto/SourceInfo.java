package com.flenski.assistant.dto;

public class SourceInfo {

    private String identifier;
    private String date;
    private String url;
    private String indexingDate;
    private String type;
    private String name;
    private String discoveryDateTime;
    private String distance;
    private String hash;

    public SourceInfo(String url, String identifier, String type, String name, String date, String discoveryDateTime, String distance, String hash) {
        this.url = url;
        this.identifier = identifier;
        this.type = type;
        this.name = name;
        this.date = date;
        this.discoveryDateTime = discoveryDateTime;
        this.distance = distance;
        this.hash = hash;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

    public String getIndexingDate() {
        return indexingDate;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDiscoveryDateTime() {
        return discoveryDateTime;
    }

    public String getDistance() {
        return distance;
    }

    public String getHash() {
        return hash;
    }
}


package com.flenski.assistant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SourceInfo {


    @JsonProperty("source_identifier")
    private String identifier;

    @JsonProperty("source_date_time")
    private String date;

    @JsonProperty("source_url")
    private String url;

    private String indexingDate;

    private String type;

    private String name;

    @JsonProperty("discovery_date_time")
    private String discoveryDateTime;

    private String distance;

    private String hash;

    private String content;


    public SourceInfo(
        @JsonProperty("source_url") String url,
        @JsonProperty("source_identifier") String identifier,
        @JsonProperty("type") String type,
        @JsonProperty("name") String name,
        @JsonProperty("source_date_time") String date,
        @JsonProperty("discovery_date_time") String discoveryDateTime,
        @JsonProperty("distance") String distance,
        @JsonProperty("hash") String hash,
        @JsonProperty("content") String content
    ) {
        this.url = url;
        this.identifier = identifier;
        this.type = type;
        this.name = name;
        this.date = date;
        this.discoveryDateTime = discoveryDateTime;
        this.distance = distance;
        this.hash = hash;
        this.content = content;
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

    public String getContent() {
        return content;

    }
}

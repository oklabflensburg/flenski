package com.flenski.dto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points;
import org.hibernate.validator.constraints.URL;

import com.flenski.type.SourceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class DocumentDto {

    @NotBlank
    private String identifier;

    private String name;

    @NotBlank
    @URL
    private String url;

    private Instant sourceDateTime;

    private String title;

    private String description;

    private String summary;

    private String group;

    private String[] categories;

    @NotNull
    private Instant discoveryDateTime;

    @NotNull
    private SourceType type;

    private String content;

    public String createHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = "";
            if (type == SourceType.PDF) {
                input = url;
            } else {
                if (content == null) {
                    throw new IllegalStateException("Content cannot be null for non-PDF documents");
                }
                input = identifier + content;
            }
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error creating hash for record", e);
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getSourceDateTime() {
        return sourceDateTime;
    }

    public void setSourceDateTime(Instant sourceDateTime) {
        this.sourceDateTime = sourceDateTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Instant getDiscoveryDateTime() {
        return discoveryDateTime;
    }

    public void setDiscoveryDateTime(Instant discoveryDateTime) {
        this.discoveryDateTime = discoveryDateTime;
    }

    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
    }
    public String[] getCategories() {
        return categories;
    }

    public String getCategoriesAsString() {
        if (categories == null) {
            return "";
        }
        return String.join(", ", categories);
    }
    public void setCategories(String[] categories) {
        this.categories = categories;       
    }
    public static DocumentDto fromScoredPoint(Points.ScoredPoint scoredPoint) {
        Map<String, Value> payload = scoredPoint.getPayloadMap();
        DocumentDto dto = new DocumentDto();
        Value value;
        value = payload.get("identifier");
        dto.setIdentifier(value!= null ? value.getStringValue() : null);
        value = payload.get("name");
        dto.setName(value!= null ? value.getStringValue() : null);
        value = payload.get("url");
        dto.setUrl(value!= null ? value.getStringValue() : null);
        value = payload.get("title");
        dto.setTitle(value!= null ? value.getStringValue() : null);
        value = payload.get("description");
        dto.setDescription(value!= null ? value.getStringValue() : null);
        value = payload.get("summary");
        dto.setSummary(value!= null ? value.getStringValue() : null);
        value = payload.get("group");
        dto.setGroup(value!= null ? value.getStringValue() : null);
        value = payload.get("content");
        dto.setContent(value!= null ? value.getStringValue() : null);

        value = payload.get("categories");
        if (value != null) {
            if (value.hasListValue()) {
                List<Value> list = value.getListValue().getValuesList();
                String[] categories = list.stream()
                        .map(Value::getStringValue)
                        .toArray(String[]::new);
                dto.setCategories(categories);
            } else if (!value.getStringValue().isEmpty()) {
                dto.setCategories(new String[]{value.getStringValue()});
            }
        }

        value = payload.get("discovery_date_time");
        if (value!= null && !value.getStringValue().isEmpty()) {
            dto.setDiscoveryDateTime(java.time.Instant.parse(value.getStringValue()));
        }
        value = payload.get("source_date_time");
        if (value != null && !value.getStringValue().isEmpty()) {
            dto.setSourceDateTime(java.time.Instant.parse(value.getStringValue()));
        }
        value = payload.get("type");
        if (value != null && !value.getStringValue().isEmpty()) {
            dto.setType(com.flenski.type.SourceType.valueOf(value.getStringValue()));
        }
        return dto;
    }
}
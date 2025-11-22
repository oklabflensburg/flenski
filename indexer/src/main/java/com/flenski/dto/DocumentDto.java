package com.flenski.dto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

import org.hibernate.validator.constraints.URL;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.flenski.type.SourceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DocumentDto {

    @NotBlank
    private String sourceIdentifier;

    private String sourceName;

    @NotBlank
    @URL
    private String sourceUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant sourceDateTime;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant discoveryDateTime;

    @NotNull
    private SourceType sourceType;

    private String content;

    public String createHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = "";
            if (sourceType == SourceType.PDF) {
                input = sourceUrl;

            } else {
                if (content == null) {
                    throw new IllegalStateException("Content cannot be null for non-PDF docuemnts");
                }
                input = sourceIdentifier + content;
            }

            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Error creating hash for record", e);
        }
    }

    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public Instant getSourceDateTime() {
        return sourceDateTime;
    }

    public Instant getDiscoveryDateTime() {
        return discoveryDateTime;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public String getContent() {
        return content;
    }

    public void setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
    }
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setSourceDateTime(Instant sourceDateTime) {
        this.sourceDateTime = sourceDateTime;
    }

    public void setDiscoveryDateTime(Instant discoveryDateTime) {
        this.discoveryDateTime = discoveryDateTime;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
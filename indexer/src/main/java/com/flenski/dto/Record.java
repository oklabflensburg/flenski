package com.flenski.dto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

import org.hibernate.validator.constraints.URL;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Record {

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
                    throw new IllegalStateException("Content cannot be null for non-PDF records");
                }
                input = sourceIdentifier + content;
            }

            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Error creating hash for record", e);
        }
    }
}

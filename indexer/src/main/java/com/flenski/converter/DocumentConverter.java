package com.flenski.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flenski.dto.DocumentDto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DocumentConverter implements AttributeConverter<DocumentDto, String> {

    private final ObjectMapper objectMapper;

    public DocumentConverter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public String convertToDatabaseColumn(DocumentDto document) {
        try {
            return objectMapper.writeValueAsString(document);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting document to JSON", e);
        }
    }

    @Override
    public DocumentDto convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, DocumentDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to record", e);
        }
    }
}
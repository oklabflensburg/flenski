package com.flenski.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flenski.dto.Record;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RecordConverter implements AttributeConverter<Record, String> {

    private final ObjectMapper objectMapper;

    public RecordConverter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public String convertToDatabaseColumn(Record record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting record to JSON", e);
        }
    }

    @Override
    public Record convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, Record.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to record", e);
        }
    }
}
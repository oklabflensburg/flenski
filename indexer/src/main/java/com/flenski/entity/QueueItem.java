package com.flenski.entity;

import java.time.LocalDateTime;

import com.flenski.converter.DocumentConverter;
import com.flenski.dto.DocumentDto;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "queue_items",
    indexes= {
        @Index(name = "idx_identifier", columnList = "identifier")
    }
)
public class QueueItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "identifier", nullable = false, updatable = false, unique = true)
    private String identifier;

    @Column(name = "record", columnDefinition = "TEXT", nullable = false, updatable = false)
    @Convert(converter = DocumentConverter.class)
    private DocumentDto record;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public DocumentDto getRecord() {
        return record;
    }

    public void setRecord(DocumentDto record) {
        this.record = record;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
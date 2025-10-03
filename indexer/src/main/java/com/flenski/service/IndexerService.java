package com.flenski.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.flenski.dto.Record;

@Service
public class IndexerService {

    private static final Logger logger = LoggerFactory.getLogger(IndexerService.class);
    private final VectorStore vectorStore;
    
    public IndexerService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void index(List<Record> records) {
        logger.info("Indexing {} records", records.size());
        
        List<Document> documents = records.stream()
                .filter(record -> StringUtils.hasText(record.getContent()))
                .map(this::mapRecordToDocument)
                .collect(Collectors.toList());
        
        if (documents.isEmpty()) {
            logger.warn("No valid documents to index (all records have empty content)");
            return;
        }
        
        vectorStore.add(documents);
        logger.info("Successfully indexed {} documents", documents.size());
    }
    
    private Document mapRecordToDocument(Record record) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceIdentifier", record.getSourceIdentifier());
        metadata.put("sourceName", record.getSourceName());
        metadata.put("sourceUrl", record.getSourceUrl());
        metadata.put("sourceType", record.getSourceType().toString());
        
        if (record.getSourceDateTime() != null) {
            metadata.put("sourceDateTime", record.getSourceDateTime().toString());
        }
        
        if (record.getDiscoveryDateTime() != null) {
            metadata.put("discoveryDateTime", record.getDiscoveryDateTime().toString());
        }
        String documentId = UUID.randomUUID().toString();
        return new Document(documentId, record.getContent(), metadata);
    }
}

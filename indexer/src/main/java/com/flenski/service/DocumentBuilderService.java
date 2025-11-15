package com.flenski.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import com.flenski.dto.Record;

@Service
public class DocumentBuilderService {

    TextSplitter textSplitter;

    DocumentBuilderService() {
        this.textSplitter = TokenTextSplitter
                .builder()
                .withChunkSize(150)
                .withMaxNumChunks(300)
                .build();
    }

    public List<Document> toChunkDocuments(Record record) {

        Document document = mapRecordToDocument(record);
        return  textSplitter.split(document);
    }

    
    public Document mapRecordToDocument(Record record) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceIdentifier", record.getSourceIdentifier());
        metadata.put("sourceName", record.getSourceName());
        metadata.put("sourceUrl", record.getSourceUrl());
        metadata.put("sourceType", record.getSourceType().toString());
        metadata.put("hash", record.createHash());
        
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

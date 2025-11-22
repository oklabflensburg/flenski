package com.flenski.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import com.flenski.config.IndexingConfig;
import com.flenski.dto.DocumentDto;

@Service
public class DocumentBuilderService {

    private final TextSplitter textSplitter;
    private final IndexingConfig config;
    
    public DocumentBuilderService(IndexingConfig config) {
        this.config = config;
        this.textSplitter = TokenTextSplitter
                .builder()
                .withChunkSize(this.config.getChunkSize())
                .withMaxNumChunks(this.config.getMaxNumnChunks())
                .build();
    }

    public List<Document> toChunkDocuments(DocumentDto documentDto) {
        Document document = mapDocumentDtoToDocument(documentDto);
        return textSplitter.split(document);
    }

    public Document mapDocumentDtoToDocument(DocumentDto documentDto) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceIdentifier", documentDto.getSourceIdentifier());
        metadata.put("sourceName", documentDto.getSourceName());
        metadata.put("sourceUrl", documentDto.getSourceUrl());
        metadata.put("sourceType", documentDto.getSourceType().toString());
        metadata.put("hash", documentDto.createHash());

        if (documentDto.getSourceDateTime() != null) {
            metadata.put("sourceDateTime", documentDto.getSourceDateTime().toString());
        }

        if (documentDto.getDiscoveryDateTime() != null) {
            metadata.put("discoveryDateTime", documentDto.getDiscoveryDateTime().toString());
        }
        String documentId = UUID.randomUUID().toString();
        return new Document(documentId, documentDto.getContent(), metadata);
    }
}

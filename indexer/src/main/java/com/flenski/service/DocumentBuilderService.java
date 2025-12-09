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
        Map<String, Object> metaData = Map.of(
            "sourceIdentifier", documentDto.getSourceIdentifier(),
            "sourceName", documentDto.getSourceName(),
            "sourceUrl", documentDto.getSourceUrl(),
            "sourceType", documentDto.getSourceType().toString(),
            "sourceDateTime", documentDto.getSourceDateTime() != null ? documentDto.getSourceDateTime().toString() : "",
            "discoveryDateTime", documentDto.getDiscoveryDateTime() != null ? documentDto.getDiscoveryDateTime().toString() : ""    
        );

        String documentId = UUID.randomUUID().toString();
        return new Document(documentId, documentDto.getContent(), metaData);
    }
}

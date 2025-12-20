package com.flenski.service;

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
        if (documentDto == null) {
            System.err.println("DocumentDto is null in mapDocumentDtoToDocument");
            return null;
        }
    

        Map<String, Object> metaData = Map.of(
            "identifier", documentDto.getIdentifier() != null ? documentDto.getIdentifier() : "",
            "title", documentDto.getTitle() != null ? documentDto.getTitle() : "",
            "description", documentDto.getDescription() != null ? documentDto.getDescription() : "",
            "summary", documentDto.getSummary() != null ? documentDto.getSummary() : "",
            "url", documentDto.getUrl() != null ? documentDto.getUrl() : "",
            "type", documentDto.getType() != null ? documentDto.getType().toString() : "",
            "sourceDateTime", documentDto.getSourceDateTime() != null ? documentDto.getSourceDateTime().toString() : "",
            "discoveryDateTime", documentDto.getDiscoveryDateTime() != null ? documentDto.getDiscoveryDateTime().toString() : "",    
            "group", documentDto.getGroup() != null ? documentDto.getGroup() : "",
            "categories", documentDto.getCategoriesAsString()
        );

        String documentId = UUID.randomUUID().toString();
        return new Document(documentId, documentDto.getContent() != null ? documentDto.getContent() : "", metaData);
    }
}

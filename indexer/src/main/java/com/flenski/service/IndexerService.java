package com.flenski.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.flenski.dto.IndexResult;
import com.flenski.dto.Record;
import com.flenski.repository.QueueItemRepository;

@Service
public class IndexerService {

    private static final Logger logger = LoggerFactory.getLogger(IndexerService.class);
    private final VectorStore vectorStore;
    private final QueueItemRepository queueItemRepository;

    public IndexerService(VectorStore vectorStore, QueueItemRepository queueItemRepository) {
        this.vectorStore = vectorStore;
        this.queueItemRepository = queueItemRepository;
    }

    public IndexResult index(Record record) {
        return index(List.of(record));
    }

    public IndexResult index(List<Record> records) {
        if (records.isEmpty()) {
            logger.info("No new records to index - all records already exist");
            return new IndexResult(records.size(), 0, 0, 0);
        }

        return new IndexResult(records.size(), 0, 0, 0);
    }

    private Document mapRecordToDocument(Record record) {
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

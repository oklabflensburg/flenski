package com.flenski.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.flenski.dto.DocumentDto;
import com.flenski.repository.QueueItemRepository;
import com.flenski.result.IndexResult;

@Service
public class IndexerService {

    private static final Logger logger = LoggerFactory.getLogger(IndexerService.class);
    private final VectorStore vectorStore;
    private final QueueItemRepository queueItemRepository;
    private final DocumentBuilderService documentBuilderService;


    public IndexerService(VectorStore vectorStore, QueueItemRepository queueItemRepository, DocumentBuilderService documentBuilderService) {
        this.vectorStore = vectorStore;
        this.queueItemRepository = queueItemRepository;
        this.documentBuilderService = documentBuilderService;
    }

    public IndexResult index(DocumentDto document) {

        List<Document> documents = documentBuilderService.toChunkDocuments(document);
    

        /**
         * TODO: move the indexing logic here from IndexController
         */
        
         return new IndexResult(0, 0, 0, 0);
    }

    public IndexResult index(List<Record> records) {
        if (records.isEmpty()) {
            logger.info("No new records to index - all records already exist");
            return new IndexResult(records.size(), 0, 0, 0);
        }

        return new IndexResult(records.size(), 0, 0, 0);
    }
}

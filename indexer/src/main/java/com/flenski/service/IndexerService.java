package com.flenski.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.flenski.dto.Record;

import  io.qdrant.client.QdrantClient;

@Service
public class IndexerService {

    private static final Logger logger = LoggerFactory.getLogger(IndexerService.class);
    private final QdrantClient vectorStoreClient;
    
    public IndexerService(QdrantClient vectorStoreClient) {
        this.vectorStoreClient = vectorStoreClient;
    }

    public void index(List<Record> Record) {
        //TODO: Implement indexing.
        logger.info("Indexing record");
    }
}

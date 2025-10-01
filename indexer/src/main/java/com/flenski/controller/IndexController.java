package com.flenski.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flenski.dto.Record;
import com.flenski.service.IndexerService;

@RestController
@RequestMapping("/api")
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    private final IndexerService indexerService;

    public IndexController(IndexerService indexerService) {
        this.indexerService = indexerService;
    }

    @PostMapping(value = "/index", consumes = "application/json")
    public ResponseEntity<String> index(@RequestBody List<Record> records) {
        logger.info("Received POST request to /api/index with {} records", records.size());
        
        if (!records.isEmpty()) {
            Record firstRecord = records.get(0);
            logger.info("First record - sourceIdentifier: {}, sourceType: {}", 
                firstRecord.getSourceIdentifier(), firstRecord.getSourceType());
        }
        
        // TODO: Call IndexerService to process records
        indexerService.index(records);
        String response = "Received " + records.size() + " records for indexing";
        logger.info("Sending response: {}", response);
        
        return ResponseEntity.ok(response);
    }
}

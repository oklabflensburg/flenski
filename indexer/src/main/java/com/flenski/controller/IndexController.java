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
import com.flenski.service.PdfConverterService;
import com.flenski.service.QueueService;

@RestController
@RequestMapping("/api")
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    private final IndexerService indexerService;
    private final PdfConverterService pdfConverterService;
    private final QueueService queueService;

    public IndexController(IndexerService indexerService, PdfConverterService pdfConverterService, QueueService queueService) {
        this.indexerService = indexerService;
        this.pdfConverterService = pdfConverterService;
        this.queueService = queueService;
    }

    @PostMapping(value = "/index", consumes = "application/json")
    public ResponseEntity<String> index(@RequestBody List<Record> records) {
        logger.info("Received POST request to /api/index with {} records", records.size());
        
        if (!records.isEmpty()) {
            /* 
            Record firstRecord = records.get(0);
            Record convertedRecord = pdfConverterService.index(firstRecord.getSourceUrl());
            IndexResult result = indexerService.index(convertedRecord);
            
            logger.info("Indexing completed - processed: {}, skipped: {}, indexed: {}", 
                       result.getProcessedRecords(), result.getSkippedRecords(), result.getIndexedDocuments());
            */

            var queueResult = queueService.add(records);
            String response = String.format("Queueing completed - added: %d, duplicates: %d", 
                                            queueResult.getAdded(), queueResult.getDuplicates());
            logger.info(response);
            
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.ok("No records received for indexing");
    }
}

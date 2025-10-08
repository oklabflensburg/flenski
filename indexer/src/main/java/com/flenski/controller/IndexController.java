package com.flenski.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flenski.dto.QueueResult;
import com.flenski.dto.Record;
import com.flenski.entity.QueueItem;
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

    @PostMapping(value = "/queue", consumes = "application/json")
    public ResponseEntity<String> queue(@RequestBody List<Record> records) {
        logger.info("Received POST request to /api/queue with {} records", records.size());

        if (!records.isEmpty()) {
            QueueResult queueResult = queueService.add(records);
            String response = String.format("Queueing completed - added: %d, duplicates: %d",
                    queueResult.getAdded(), queueResult.getDuplicates());
            logger.info(response);

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok("No records received for queuing");
    }

    @GetMapping(value = "/index")
    public ResponseEntity<String> index() {
        logger.info("Received GET request to /api/index");

        List<QueueItem> queueItems = queueService.getNext(10);

        if (!queueItems.isEmpty()) {
            for (int i = 0; i < queueItems.size(); i++) {
                QueueItem queueItem = queueItems.get(i);
                Record record = queueItem.getRecord();
                try {
                    Record convertedRecord = pdfConverterService.index(record.getSourceUrl());
                    indexerService.index(convertedRecord);
                    queueService.delete(queueItem);

                } catch (Exception e) {
                    logger.error("Error processing record: {}", record.getSourceUrl(), e);
                }
            };
        }

        String response = "Indexing completed";
        return ResponseEntity.ok(response);
    }
}
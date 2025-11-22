package com.flenski.controller;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flenski.dto.Point;
import com.flenski.dto.Record;
import com.flenski.dto.Vector;
import com.flenski.entity.QueueItem;
import com.flenski.request.HybridFusionSearchRequest;
import com.flenski.request.IndexRequest;
import com.flenski.result.QueueResult;
import com.flenski.service.DenseVectorService;
import com.flenski.service.DocumentBuilderService;
import com.flenski.service.IndexerService;
import com.flenski.service.PdfConverterService;
import com.flenski.service.QueueService;
import com.flenski.service.SparseVectorService;
import com.flenski.type.SourceType;

@RestController
@RequestMapping("/api")
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    private final IndexerService indexerService;
    private final PdfConverterService pdfConverterService;
    private final QueueService queueService;
    private final SparseVectorService sparseVectorService;
    private final DenseVectorService denseVectorService;
    private final DocumentBuilderService documentBuilderService;
    private final HybridFusionSearchRequest hybridFusionSearchRequest;
    private final HttpClient httpClient;

    public IndexController(
            IndexerService indexerService,
            PdfConverterService pdfConverterService,
            QueueService queueService,
            SparseVectorService sparseVectorService,
            DenseVectorService denseVectorService,
            DocumentBuilderService documentBuilderService,
            HybridFusionSearchRequest hybridFusionSearchRequest
    ) {
        this.indexerService = indexerService;
        this.pdfConverterService = pdfConverterService;
        this.queueService = queueService;
        this.sparseVectorService = sparseVectorService;
        this.denseVectorService = denseVectorService;
        this.documentBuilderService = documentBuilderService;
        this.hybridFusionSearchRequest = hybridFusionSearchRequest;
        this.httpClient = HttpClient.newHttpClient();
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

    @GetMapping(value = "/point")
    public ResponseEntity<String> point() {
        List<QueueItem> queueItems = queueService.getNext(200);

        int id = 1;
        for (QueueItem item : queueItems) {
            try {
                logger.info("Queue Item ID: {}, Record Source URL: {}", item.getId(), item.getRecord().getSourceUrl());

                if (queueItems.isEmpty()) {
                    return ResponseEntity.status(404).body("No queue items available");
                }
                Record record = item.getRecord();
                List<Document> documents = documentBuilderService.toChunkDocuments(record);
                for (Document doc : documents) {
                    try {
                        Vector denseVector = denseVectorService.embed(doc);
                        denseVector.setName("dense");

                        Vector sparseVector = sparseVectorService.embed(doc.getText());
                        sparseVector.setName("sparse");

                        Point point = new Point();
                        point.setId(id);
                        point.addVector(sparseVector);
                        point.addVector(denseVector);
                        point.addToPayload("source_url", record.getSourceUrl());
                        point.addToPayload("content", doc.getText());
                        point.addToPayload("source_identifier", record.getSourceIdentifier());
                        point.addToPayload("discovery_date_time", record.getDiscoveryDateTime().toString());
                        point.addToPayload("source_date_time", record.getSourceDateTime().toString());
                        IndexRequest indexRequest = new IndexRequest();
                        indexRequest.addPoint(point);
                        HttpRequest request = indexRequest.build();

                        logger.info("Sending request for point with id : {}", id);
                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        logger.info("Qdrant response: {}", response.body());
                    } catch (Throwable t) {
                        logger.error("Error sending request to Qdrant: {}", t.getMessage(), t);
                    }
                    id++;
                }
            } catch (Throwable t) {
                logger.error("Error processing queue item: {}", item.getId(), t);
            }
        }

        return ResponseEntity.ok("ok");
    }

    @GetMapping(value = "/index")
    public ResponseEntity<String> index() {
        logger.info("Received GET request to /api/index");

        List<QueueItem> queueItems = queueService.getNext(50);

        if (!queueItems.isEmpty()) {
            for (int i = 0; i < queueItems.size(); i++) {
                QueueItem queueItem = queueItems.get(i);
                try {
                    Record record = queueItem.getRecord();
                    try {
                        if (record.getSourceType() == SourceType.PDF) {
                            logger.info("Converting PDF record: {}", record.getSourceUrl());
                            Record convertedRecord = pdfConverterService.index(record.getSourceUrl());
                            indexerService.index(convertedRecord);
                        } else {
                            indexerService.index(record);
                        }
                        //  queueService.delete(queueItem);
                    } catch (Throwable t) {
                        logger.error("Error processing record: {}", record.getSourceUrl(), t);
                    }
                } catch (Throwable t) {
                    logger.error("Error processing queue item: {}", queueItem.getId(), t);
                }
            }
        }

        String response = "Indexing completed";
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<String> search(@RequestParam(name = "q") String q) {

        hybridFusionSearchRequest.setQueryText(q, 10);
        HttpRequest request = hybridFusionSearchRequest.build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
             return ResponseEntity.ok(response.body());
        } catch (Throwable e) {
            throw new RuntimeException("Failed to execute search request", e);
        }
    }
}

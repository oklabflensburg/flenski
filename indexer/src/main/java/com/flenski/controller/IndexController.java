package com.flenski.controller;

import java.net.URI;
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
import com.flenski.dto.QueueResult;
import com.flenski.dto.Record;
import com.flenski.dto.SourceType;
import com.flenski.dto.Vector;
import com.flenski.dto.IndexRequest;
import com.flenski.entity.QueueItem;
import com.flenski.service.DenseVectorService;
import com.flenski.service.DocumentBuilderService;
import com.flenski.service.IndexerService;
import com.flenski.service.PdfConverterService;
import com.flenski.service.QueueService;
import com.flenski.service.SparseVectorService;

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

    public IndexController(
            IndexerService indexerService,
            PdfConverterService pdfConverterService,
            QueueService queueService,
            SparseVectorService sparseVectorService,
            DenseVectorService denseVectorService,
            DocumentBuilderService documentBuilderService
    ) {

        this.indexerService = indexerService;
        this.pdfConverterService = pdfConverterService;
        this.queueService = queueService;
        this.sparseVectorService = sparseVectorService;
        this.denseVectorService = denseVectorService;
        this.documentBuilderService = documentBuilderService;
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
        List<QueueItem> queueItems = queueService.getNext(1);
        if (queueItems.isEmpty()) {
            return ResponseEntity.status(404).body("No queue items available");
        }
        Record record = queueItems.get(0).getRecord();
        List<Document> documents = documentBuilderService.toChunkDocuments(record);
        Document doc = documents.get(0);
        Vector denseVector = denseVectorService.embed(doc);
        denseVector.setName("dense");
        
        Vector sparseVector = sparseVectorService.vectorize(doc.getText(), 1.2, 0.75, 100.0);
        sparseVector.setName("sparse");
    
        Point point = new Point();
        point.setId(1);
        point.addVector(sparseVector);
        point.addVector(denseVector);
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.addPoint(point);
        HttpRequest request = indexRequest.build();

        HttpClient client = HttpClient.newHttpClient();

        try {
            String requestBody = indexRequest.toJson();
            logger.info("Sending request with body: {}", requestBody);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Qdrant response: {}", response.body());
        } catch (Exception e) {
            logger.error("Error sending request to Qdrant: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok("ok");
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
                    if (record.getSourceType() == SourceType.PDF) {
                        logger.info("Converting PDF record: {}", record.getSourceUrl());
                        Record convertedRecord = pdfConverterService.index(record.getSourceUrl());
                        indexerService.index(convertedRecord);
                    } else {
                        indexerService.index(record);
                    }

                    //  queueService.delete(queueItem);
                } catch (Exception e) {
                    logger.error("Error processing record: {}", record.getSourceUrl(), e);
                }
            }
        }

        String response = "Indexing completed";
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/sparse-vector")
    public ResponseEntity<String> getSparseVector(@RequestParam("q") String query) {
        logger.info("Received GET request to /api/sparseVector with query: {}", query);
        SparseVectorService.SparseVector vector = sparseVectorService.vectorize(query, 1.2, 0.75, 100.0);

        String indicesString = java.util.Arrays.toString(vector.indices());
        String valuesString = java.util.Arrays.toString(vector.values());
        String json = String.format("{\n  \"indices\": %s,\n  \"values\": %s\n}", indicesString, valuesString);
        logger.info(json);

        return ResponseEntity.ok(json);
    }

    @GetMapping(value = "/createSparseVector")
    public ResponseEntity<String> sparse() {
        List<QueueItem> queueItems = queueService.getNext(10);

        String response = "";

        if (!queueItems.isEmpty()) {
            for (int i = 0; i < queueItems.size(); i++) {
                QueueItem queueItem = queueItems.get(i);
                Record record = queueItem.getRecord();
                try {
                    SparseVectorService.SparseVector vector = sparseVectorService.vectorize(
                            record.getContent(), 1.2, 0.75, 100.0);

                    String indicesString = java.util.Arrays.toString(vector.indices());
                    String valuesString = java.util.Arrays.toString(vector.values());
                    String json = String.format("{\n  \"indices\": %s,\n  \"values\": %s\n}", indicesString, valuesString);
                    logger.info("Created sparse vector for record: {}", record.getSourceUrl());
                    logger.info(json);
                    response += json + "\n";

                    /*queueService.delete(queueItem);*/
                } catch (Exception e) {
                    logger.error("Error processing record: {}", record.getSourceUrl(), e);
                }
            }
        }
        return ResponseEntity.ok(response);
    }
}

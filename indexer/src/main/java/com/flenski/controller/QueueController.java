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
import com.flenski.result.QueueResult;
import com.flenski.service.QueueService;

@RestController
@RequestMapping("api")
public class QueueController {

    private final Logger log = LoggerFactory.getLogger(QueueController.class);
    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping(value = "/queue", consumes = "application/json")
    public ResponseEntity<String> add(@RequestBody List<Record> records) {
        log.info("Adding item to queue");

        if (records.isEmpty()) {
            return ResponseEntity.badRequest().body("No records provided");
        }

        QueueResult queueResult = queueService.add(records);
        String response = String.format("Queueing completed - added: %d, duplicates: %d",
                queueResult.getAdded(), queueResult.getDuplicates());
        log.info(response);

        return ResponseEntity.ok(response);
    }
}

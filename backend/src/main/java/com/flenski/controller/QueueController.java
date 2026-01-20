package com.flenski.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flenski.config.IndexingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flenski.dto.DocumentDto;
import com.flenski.result.QueueResult;
import com.flenski.service.QueueService;

@RestController
@RequestMapping("api/queue")
public class QueueController {

    private final Logger log = LoggerFactory.getLogger(QueueController.class);
    private final QueueService queueService;
    private final IndexingConfig indexingConfig;


    public QueueController(QueueService queueService, IndexingConfig indexingConfig) {
        this.indexingConfig = indexingConfig;
        this.queueService = queueService;
    }

    @PostMapping(value = "/queue", consumes = "application/json")
    public ResponseEntity<String> add(@RequestBody List<DocumentDto> documents) {
        log.info("Adding item to queue");

        if (documents.isEmpty()) {
            return ResponseEntity.badRequest().body("No documents provided");
        }

        QueueResult queueResult = queueService.add(documents);
        String response = String.format("Queueing completed - added: %d, duplicates: %d",
                queueResult.getAdded(), queueResult.getDuplicates());
        log.info(response);

        return ResponseEntity.ok(response);
    }

    @Scheduled(fixedDelay = 1000)
    private void autoqueue() {
        Path sourceDirectory = Paths.get(indexingConfig.getDocumentSourceDirectoryName());
        Path queuedDirectory = Paths.get(indexingConfig.getDocumentQueuedDirectoryName());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try {
            if (!Files.exists(queuedDirectory)) {
                Files.createDirectories(queuedDirectory);
            }
            try (Stream<Path> files = Files.list(sourceDirectory)
                    .filter(
                            p -> p.toString().endsWith(".json")
                    )
            ) {
                files.forEach(path -> {
                    try {
                        List<DocumentDto> documents = Arrays.asList(
                                objectMapper.readValue(path.toFile(), DocumentDto[].class)
                        );
                        if (!documents.isEmpty()) {
                            QueueResult queueResult = queueService.add(documents);
                            log.info("Autoqueue: File {} processed - added: {}, duplicates: {}", path.getFileName(), queueResult.getAdded(), queueResult.getDuplicates());

                            Path target = queuedDirectory.resolve(path.getFileName());

                            try {
                                Files.move(path, target, StandardCopyOption.REPLACE_EXISTING);
                                log.info("File {} moved to queued directory", path.getFileName());
                            } catch (Exception moveEx) {
                                log.error("Error moving file {} to queued directory: {}", path.getFileName(), moveEx.getMessage());
                            }
                        } else {
                            log.info("Autoqueue: File {} contains no documents", path.getFileName());
                        }
                    } catch (Exception e) {
                        log.error("Error processing file {}: {}", path.getFileName(), e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error accessing directory {}: {}", indexingConfig.getDocumentSourceDirectoryName(), e.getMessage());
        }
    }
}

package com.flenski.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flenski.dto.DocumentDto;
import com.flenski.entity.QueueItem;
import com.flenski.service.IndexerService;
import com.flenski.service.QueueService;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.CreateCollection;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.SparseVectorConfig;
import io.qdrant.client.grpc.Collections.SparseVectorParams;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Collections.VectorParamsMap;
import io.qdrant.client.grpc.Collections.VectorsConfig;
import io.qdrant.client.grpc.Collections;

@RestController
@RequestMapping("/api")
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    private final QdrantClient client;
    private final QueueService queueService;
    private final IndexerService indexerService;

    public IndexController(QueueService queueService, IndexerService indexerService) {
        this.queueService = queueService;
        this.indexerService = indexerService;
        this.client = new QdrantClient(
                QdrantGrpcClient.newBuilder("localhost", 6334, false).build()
        );
    }

    @PostMapping("collection")
    public ResponseEntity<String> createCollection() throws InterruptedException, ExecutionException {

        this.client.createCollectionAsync(
                CreateCollection.newBuilder()
                        .setCollectionName("test")
                        .setVectorsConfig(VectorsConfig.newBuilder().setParamsMap(
                                VectorParamsMap.newBuilder().putAllMap(
                                        Map.of(
                                                "dense",
                                                VectorParams.newBuilder()
                                                        .setSize(1536)
                                                        .setDistance(Distance.Cosine)
                                                        .setDatatype(Collections.Datatype.Float32)
                                                        .setHnswConfig(
                                                                Collections.HnswConfigDiff.newBuilder()
                                                                        .setM(24)
                                                                        .setEfConstruct(256)
                                                                        .setPayloadM(24)
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                )
                        )
                        )
                        .setSparseVectorsConfig(SparseVectorConfig.newBuilder().putMap(
                                "sparse", SparseVectorParams.getDefaultInstance()))
                        .build())
                .get();

        return ResponseEntity.ok("Collection created");
    }

    @PostMapping("/point")
    ResponseEntity<String> upsertPoint() {

        List<QueueItem> queueItems = queueService.getNext(5);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (QueueItem queueItem : queueItems) {
            try {
                DocumentDto documentDto = queueItem.getRecord();
                try {
                    CompletableFuture<Void> future = indexerService.prepareDocumentForIndexing(documentDto)
                            .thenCompose(preparedDocument -> indexerService.upsert(preparedDocument))
                            .thenAccept(indexResult -> {
                                logger.info("Indexed document: {}", documentDto.getSourceUrl());
                            })
                            .exceptionally(e -> {
                                logger.error("Error indexing document: {}", documentDto.getSourceUrl(), e);
                                return null;
                            });

                    futures.add(future);

                    //  queueService.delete(queueItem);
                } catch (Throwable t) {
                    logger.error("Error processing record: {}", documentDto.getSourceUrl(), t);
                }
            } catch (Throwable t) {
                logger.error("Error processing queue item: {}", queueItem.getId(), t);
            }

        }

        return ResponseEntity.ok("ok");
    }
}

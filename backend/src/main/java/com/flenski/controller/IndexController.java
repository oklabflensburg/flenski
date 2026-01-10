package com.flenski.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flenski.config.VectorStoreClientConfig;
import com.flenski.dto.DocumentDto;
import com.flenski.entity.QueueItem;
import com.flenski.service.IndexerService;
import com.flenski.service.QueueService;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Collections.CreateCollection;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.PayloadSchemaType;
import io.qdrant.client.grpc.Collections.SparseVectorConfig;
import io.qdrant.client.grpc.Collections.SparseVectorParams;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Collections.VectorParamsMap;
import io.qdrant.client.grpc.Collections.VectorsConfig;
import com.flenski.config.IndexingConfig;

@RestController
@RequestMapping("/api")
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    private final QdrantClient client;
    private final QueueService queueService;
    private final IndexerService indexerService;
    private final VectorStoreClientConfig vectorStoreClientConfig;
    private final IndexingConfig indexingConfig;

    public IndexController(QueueService queueService, IndexerService indexerService, VectorStoreClientConfig vectorStoreClientConfig, IndexingConfig indexingConfig) {
        this.queueService = queueService;
        this.indexerService = indexerService;
        this.vectorStoreClientConfig = vectorStoreClientConfig;
        this.indexingConfig = indexingConfig;
        this.client = new QdrantClient(
                QdrantGrpcClient.newBuilder(vectorStoreClientConfig.getHost(), vectorStoreClientConfig.getPort(), false).build()
        );
    }

    @PostMapping("collection")
    public ResponseEntity<String> createCollection() throws InterruptedException, ExecutionException {

        this.client.createCollectionAsync(
                        CreateCollection.newBuilder()
                                .setCollectionName(this.vectorStoreClientConfig.getCollectionName())
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

        this.client.createPayloadIndexAsync(
                this.vectorStoreClientConfig.getCollectionName(),
                "categories",
                PayloadSchemaType.Text,
                null,
                true,
                null,
                null);

        this.client.createPayloadIndexAsync(
                this.vectorStoreClientConfig.getCollectionName(),
                "source_date_time",
                PayloadSchemaType.Datetime,
                null,
                true,
                null,
                null);

        this.client.createPayloadIndexAsync(
                this.vectorStoreClientConfig.getCollectionName(),
                "title",
                PayloadSchemaType.Text,
                Collections.PayloadIndexParams.newBuilder()
                        .setTextIndexParams(
                                Collections.TextIndexParams.newBuilder()
                                        .setTokenizer(Collections.TokenizerType.Word)
                                        .setLowercase(true)
                                        .setMinTokenLen(2)
                                        .setMaxTokenLen(10)
                                        .setPhraseMatching(true)
                                        .setAsciiFolding(true)
                                        .setStemmer(
                                                Collections.StemmingAlgorithm.newBuilder()
                                                        .setSnowball(
                                                                Collections.SnowballParams.newBuilder().setLanguage("german").build())
                                                        .build())
                                        .setStopwords(
                                                Collections.StopwordsSet.newBuilder()
                                                        .addAllLanguages(List.of("german", "english", "danish"))
                                                        .build())
                                        .build())
                        .build(),
                true,
                null,
                null);

        this.client.createPayloadIndexAsync(
                this.vectorStoreClientConfig.getCollectionName(),
                "content",
                PayloadSchemaType.Text,
                Collections.PayloadIndexParams.newBuilder()
                        .setTextIndexParams(
                                Collections.TextIndexParams.newBuilder()
                                        .setTokenizer(Collections.TokenizerType.Word)
                                        .setLowercase(true)
                                        .setMinTokenLen(2)
                                        .setMaxTokenLen(10)
                                        .setPhraseMatching(true)
                                        .setAsciiFolding(true)
                                        .setStemmer(
                                                Collections.StemmingAlgorithm.newBuilder()
                                                        .setSnowball(
                                                                Collections.SnowballParams.newBuilder().setLanguage("german").build())
                                                        .build())
                                        .setStopwords(
                                                Collections.StopwordsSet.newBuilder()
                                                        .addAllLanguages(List.of("german", "english", "danish"))
                                                        .build())
                                        .build())
                        .build(),
                true,
                null,
                null);


        this.client.createPayloadIndexAsync(
                this.vectorStoreClientConfig.getCollectionName(),
                "group",
                PayloadSchemaType.Keyword,
                null,
                true,
                null,
                null);

        return ResponseEntity.ok("Collection created");
    }

    // Runs every 10 seconds
    @Scheduled(fixedDelay = 10000)
    private void upsertPoint() {
        List<QueueItem> queueItems = queueService.getNext(indexingConfig.getQueueBatchSize());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (QueueItem queueItem : queueItems) {
            try {
                DocumentDto documentDto = queueItem.getDocument();
                try {
                    CompletableFuture<Void> future = indexerService.prepareDocumentForIndexing(documentDto)
                            .thenCompose(preparedDocument -> indexerService.upsert(preparedDocument))
                            .thenAccept(indexResult -> {
                                logger.info("Indexed document: {}", documentDto.getUrl());
                            })
                            .exceptionally(e -> {
                                logger.error("Error indexing document: {}", documentDto.getUrl(), e);
                                return null;
                            });

                    futures.add(future);

                    queueService.delete(queueItem);
                } catch (Throwable t) {
                    logger.error("Error processing record: {}", documentDto.getUrl(), t);
                }
            } catch (Throwable t) {
                logger.error("Error processing queue item: {}", queueItem.getId(), t);
            }
        }
    }
}

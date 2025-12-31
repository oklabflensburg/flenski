package com.flenski.controller;

import com.flenski.config.VectorStoreClientConfig;
import com.flenski.dto.DocumentDto;
import com.flenski.dto.QueryResponseDto;
import com.flenski.queryTransformers.CompressionTransformer;
import com.flenski.service.DenseVectorService;
import com.flenski.service.SparseVectorService;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Points;
import io.qdrant.client.grpc.Points.QueryPoints;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.qdrant.client.QueryFactory.fusion;
import static io.qdrant.client.QueryFactory.nearest;

@RestController
@RequestMapping("/api")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final QdrantClient client;
    private final SparseVectorService sparseVectorService;
    private final VectorStoreClientConfig vectorStoreClientConfig;
    private final DenseVectorService denseVectorService;
    private final CompressionTransformer compressionTransformer;
    private final ChatClient chatClient;

    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource systemPromptTemplate;

    public ChatController(
            VectorStoreClientConfig vectorStoreClientConfig,
            SparseVectorService sparseVectorService,
            DenseVectorService denseVectorService,
            CompressionTransformer compressionTransformer,
            ChatClient chatClient
    ) {
        logger.info("Initializing ChatController with host: {} and port: {}", vectorStoreClientConfig.getHost(), vectorStoreClientConfig.getPort());
        this.client = new QdrantClient(
                QdrantGrpcClient.newBuilder(vectorStoreClientConfig.getHost(), vectorStoreClientConfig.getPort(), false).build()
        );
        this.vectorStoreClientConfig = vectorStoreClientConfig;
        this.sparseVectorService = sparseVectorService;
        this.denseVectorService = denseVectorService;
        this.compressionTransformer = compressionTransformer;
        this.chatClient = chatClient;
    }

    @GetMapping("sparsequery")
    public ResponseEntity<List<DocumentDto>> sparsequery(@RequestParam("q") String message) throws Exception {
        logger.info("Received sparsequery request with message: {}", message);
        Points.SparseVector sparseVector = sparseVectorService.embed(message);

        List<Points.ScoredPoint> scoredPoints = this.client.queryAsync(QueryPoints.newBuilder()
                        .setCollectionName(vectorStoreClientConfig.getCollectionName())
                        .addPrefetch(Points.PrefetchQuery.newBuilder()
                                .setQuery(nearest(sparseVector.getValuesList(), sparseVector.getIndicesList()))
                                .setUsing("sparse")
                                .setLimit(100)
                                .build())
                        .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                        .setQuery(fusion(Points.Fusion.RRF))
                        .build()
                )
                .get();
        logger.info("sparsequery returned {} results", scoredPoints.size());
        List<DocumentDto> documents = scoredPoints.stream().map(DocumentDto::fromScoredPoint).toList();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("hybridquery")
    public ResponseEntity<QueryResponseDto> hybridquery(@RequestParam("q") String message) throws Exception {
        logger.info("Received hybridquery request with message: {}", message);
        String transformedMessage = compressionTransformer.transform(message);
        logger.debug("Transformed message: {}", transformedMessage);

        Points.SparseVector sparseVector = sparseVectorService.embed(transformedMessage);
        Points.DenseVector denseVector = denseVectorService.embed(transformedMessage);

        List<Points.ScoredPoint> scoredPoints = this.client.queryAsync(QueryPoints.newBuilder()
                        .setCollectionName(vectorStoreClientConfig.getCollectionName())
                        .addPrefetch(Points.PrefetchQuery.newBuilder()
                                .setQuery(nearest(sparseVector.getValuesList(), sparseVector.getIndicesList()))
                                .setUsing("sparse")
                                .setLimit(100)
                                .build())
                        .addPrefetch(Points.PrefetchQuery.newBuilder()
                                .setQuery(nearest(denseVector.getDataList()))
                                .setUsing("dense")
                                .setLimit(5)
                                .build())
                        .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                        .setQuery(fusion(Points.Fusion.RRF))
                        .build()
                )
                .get();
        logger.info("hybridquery returned {} results", scoredPoints.size());
        List<DocumentDto> documents = scoredPoints.stream().map(DocumentDto::fromScoredPoint).toList();
        final StringBuilder documentContext = new StringBuilder();
        int maxContextDocuments = 5;
        for (DocumentDto document : documents) {
            documentContext.append(document.toString());
            if (--maxContextDocuments <= 0) {
                break;
            }
        }

        String answer = chatClient.prompt()
                .system(promptTemplateSpec -> promptTemplateSpec
                        .text(systemPromptTemplate)
                        .param("documents", documentContext.toString())
                )
                .user(message)
                .call()
                .content();

        logger.info("Generated answer: {}", answer);
        QueryResponseDto response = new QueryResponseDto(answer, documents);
        return ResponseEntity.ok(response);
    }

    @GetMapping("hybridquery-stream")
    public SseEmitter hybridqueryStream(@RequestParam("q") String message) throws Exception {
        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                String transformedMessage = compressionTransformer.transform(message);
                Points.SparseVector sparseVector = sparseVectorService.embed(transformedMessage);
                Points.DenseVector denseVector = denseVectorService.embed(transformedMessage);
                List<Points.ScoredPoint> scoredPoints = this.client.queryAsync(QueryPoints.newBuilder()
                        .setCollectionName(vectorStoreClientConfig.getCollectionName())
                        .addPrefetch(Points.PrefetchQuery.newBuilder()
                                .setQuery(nearest(sparseVector.getValuesList(), sparseVector.getIndicesList()))
                                .setUsing("sparse")
                                .setLimit(100)
                                .build())
                        .addPrefetch(Points.PrefetchQuery.newBuilder()
                                .setQuery(nearest(denseVector.getDataList()))
                                .setUsing("dense")
                                .setLimit(5)
                                .build())
                        .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                        .setQuery(fusion(Points.Fusion.RRF))
                        .build()
                ).get();
                List<DocumentDto> documents = scoredPoints.stream().map(DocumentDto::fromScoredPoint).toList();
                emitter.send(SseEmitter.event().name("documents").data(documents));

                final StringBuilder documentContext = new StringBuilder();
                int maxContextDocuments = 5;
                for (DocumentDto document : documents) {
                    documentContext.append(document.toString());
                    if (--maxContextDocuments <= 0) {
                        break;
                    }
                }
                String answer = chatClient.prompt()
                        .system(promptTemplateSpec -> promptTemplateSpec
                                .text(systemPromptTemplate)
                                .param("documents", documentContext.toString())
                        )
                        .user(message)
                        .call()
                        .content();
                emitter.send(SseEmitter.event().name("answer").data(answer));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }
}

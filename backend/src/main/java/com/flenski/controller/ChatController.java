package com.flenski.controller;

import com.flenski.FilterConditionBuilder;
import com.flenski.config.VectorStoreClientConfig;
import com.flenski.dto.DocumentDto;
import com.flenski.queryTransformers.CompressionTransformer;
import com.flenski.queryTransformers.DateRangeTransformer;
import com.flenski.service.DenseVectorService;
import com.flenski.service.QueryService;
import com.flenski.service.SparseVectorService;
import com.flenski.value.DateRange;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Common;
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

import static io.qdrant.client.QueryFactory.*;

import static io.qdrant.client.ExpressionFactory.datetime;
import static io.qdrant.client.ExpressionFactory.datetimeKey;
import static io.qdrant.client.ExpressionFactory.expDecay;
import static io.qdrant.client.ExpressionFactory.sum;
import static io.qdrant.client.ExpressionFactory.variable;
import static io.qdrant.client.QueryFactory.formula;
import static io.qdrant.client.QueryFactory.nearest;

import io.qdrant.client.grpc.Points.DecayParamsExpression;
import io.qdrant.client.grpc.Points.Formula;
import io.qdrant.client.grpc.Points.SumExpression;


@RestController
@RequestMapping("/api/chat/")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final QdrantClient client;
    private final SparseVectorService sparseVectorService;
    private final VectorStoreClientConfig vectorStoreClientConfig;
    private final DenseVectorService denseVectorService;
    private final CompressionTransformer compressionTransformer;
    private final DateRangeTransformer dateRangeTransformer;
    private final ChatClient chatClient;
    private final QueryService queryService;

    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource systemPromptTemplate;

    public ChatController(
            VectorStoreClientConfig vectorStoreClientConfig,
            SparseVectorService sparseVectorService,
            DenseVectorService denseVectorService,
            CompressionTransformer compressionTransformer,
            DateRangeTransformer dateRangeTransformer,
            ChatClient chatClient,
            QueryService queryService
    ) {
        logger.info("Initializing ChatController with host: {} and port: {} and collection: {}", vectorStoreClientConfig.getHost(), vectorStoreClientConfig.getPort(), vectorStoreClientConfig.getCollectionName());
        this.client = new QdrantClient(
                QdrantGrpcClient
                        .newBuilder(vectorStoreClientConfig.getHost(), vectorStoreClientConfig.getPort(), false)
                        .withApiKey(vectorStoreClientConfig.getApiKey())
                        .build()
        );
        this.vectorStoreClientConfig = vectorStoreClientConfig;
        this.sparseVectorService = sparseVectorService;
        this.denseVectorService = denseVectorService;
        this.compressionTransformer = compressionTransformer;
        this.chatClient = chatClient;
        this.dateRangeTransformer = dateRangeTransformer;
        this.queryService = queryService;
    }


    @GetMapping("hybridquery-stream")
    public SseEmitter sparsequeryStream(
            @RequestParam("q") String message,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to
    ) throws Exception {
        logger.info("Received sparse query request with message: {}", message);
        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                Points.SparseVector sparseVector = sparseVectorService.embed(message);
                Points.QueryPoints queryPoints = queryService.buildSparseQueryTimeBoost(sparseVector);

                List<Points.ScoredPoint> scoredPoints = this.client.queryAsync(queryPoints).get();

                scoredPoints = scoredPoints.stream().toList();
                List<DocumentDto> documents = scoredPoints.stream().map(DocumentDto::fromScoredPoint).toList();

                logger.info("hybridquery returned {} results ", scoredPoints.size());
                emitter.send(SseEmitter.event().name("documents").data(documents));

                final StringBuilder documentContext = new StringBuilder();
                int maxContextDocuments = 100;
                for (DocumentDto document : documents) {
                    documentContext.append(document.toString());
                    if (--maxContextDocuments <= 0) {
                        break;
                    }
                }

                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }
}

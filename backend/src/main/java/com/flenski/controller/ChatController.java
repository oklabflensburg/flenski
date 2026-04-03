package com.flenski.controller;

import com.flenski.config.QueryConfig;
import com.flenski.config.VectorStoreClientConfig;
import com.flenski.dto.DocumentDto;
import com.flenski.dto.QueryParameterBag;
import com.flenski.queryTransformers.CompressionTransformer;
import com.flenski.queryTransformers.DateRangeTransformer;
import com.flenski.service.DenseVectorService;
import com.flenski.service.QueryService;
import com.flenski.service.SparseVectorService;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Points;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
    private final QueryConfig queryConfig;

    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource systemPromptTemplate;

    public ChatController(
            VectorStoreClientConfig vectorStoreClientConfig,
            SparseVectorService sparseVectorService,
            DenseVectorService denseVectorService,
            CompressionTransformer compressionTransformer,
            DateRangeTransformer dateRangeTransformer,
            ChatClient chatClient,
            QueryService queryService,
            QueryConfig queryConfig
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
        this.queryConfig = queryConfig;
    }

    @PostMapping("query")
    public SseEmitter postQuery(
            @RequestParam(value = "q") String message,
            @RequestBody QueryParameterBag queryParameterBag)
            throws Exception {

        logger.info("Received query: {}", message);
        queryParameterBag.initFromConfig(queryConfig);
        logger.info("Query parameters: {}", queryParameterBag.toString());

        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                    List<DocumentDto> documents = queryService.query(client, message, queryParameterBag, queryConfig);

                    logger.info("Query returned  {} results ", documents.size());
                    emitter.send(SseEmitter.event().name("documents").data(documents));
                    emitter.complete();

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }
}

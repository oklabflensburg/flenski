package com.flenski.controller;

import com.flenski.config.QueryConfig;
import com.flenski.config.VectorStoreClientConfig;
import com.flenski.dto.DocumentDto;
import com.flenski.dto.QueryParameterBag;
import com.flenski.queryTransformers.CompressionTransformer;
import com.flenski.queryTransformers.DateRangeTransformer;
import com.flenski.service.ChatService;
import com.flenski.service.DenseVectorService;
import com.flenski.service.QueryService;
import com.flenski.service.SparseVectorService;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private final ChatService chatService;

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
            QueryConfig queryConfig,
            ChatService chatService
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
        this.chatService = chatService;
    }

    @GetMapping("defaultParameters")
    public QueryParameterBag defaultParameters() {
        QueryParameterBag queryParameterBag = new QueryParameterBag();
        queryParameterBag.initFromConfig(queryConfig);
        return queryParameterBag;
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
                    String transformedMessage = compressionTransformer.transform(message);
                    emitter.send(SseEmitter.event().name("message").data("Suche nach: \"" + transformedMessage + "\""));

                    List<DocumentDto> documents = queryService.query(client, message, queryParameterBag, queryConfig);
                    logger.info("Query returned  {} results ", documents.size());

                    String answer = chatService.ask(message, documents);
                    emitter.send(SseEmitter.event().name("answer").data(answer));
                    logger.info("Received answer: {}", answer);

                    List<DocumentDto> documentsGrouped  = groupDocuments(documents);
                    emitter.send(SseEmitter.event().name("documents").data(documentsGrouped));
                    emitter.complete();

            } catch (Exception e) {
                this.logger.error(e.getMessage());
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

    public List<DocumentDto> groupDocuments(List<DocumentDto> documents) {

        Map<String, DocumentDto> documentsMap = new LinkedHashMap<>();
        for (DocumentDto document : documents) {
            documentsMap.merge(document.getIdentifier(), document,
                    (existing, incoming) -> existing.getScore() >= incoming.getScore() ? existing : incoming);
        }
      return new ArrayList<>(documentsMap.values());
    }
}

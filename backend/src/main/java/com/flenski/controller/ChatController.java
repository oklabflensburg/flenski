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

    @GetMapping("sparsequery")
    public ResponseEntity<List<DocumentDto>> sparsequery(
            @RequestParam("q") String message,
            @RequestParam(value = "minScore", required = false, defaultValue = "0.5") double minScore
    ) throws Exception {
        logger.info("Received sparsequery request with message: {}", message);
        Points.SparseVector sparseVector = sparseVectorService.embed(message);


        List<Points.ScoredPoint> scoredPoints = this.client.queryAsync(QueryPoints.newBuilder()
                        .setCollectionName(vectorStoreClientConfig.getCollectionName())
                        .setLimit(100)
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
        scoredPoints = scoredPoints.stream()
                .filter(point -> point.getScore() >= minScore)
                .toList();
        logger.info("sparsequery returned {} results above threshold {}", scoredPoints.size(), minScore);
        List<DocumentDto> documents = scoredPoints.stream().map(DocumentDto::fromScoredPoint).toList();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("hybridquery-stream1")
    public SseEmitter hybridqueryStream1(
            @RequestParam("q") String message,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to
    ) throws Exception {
        logger.info("Received hybridquery request with message: {}", message);
        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                String transformedMessage = compressionTransformer.transform(message);
                logger.info("Transformed message: {}", transformedMessage);

                DateRange dateRange;
                if (from != null || to != null) {
                    dateRange = new DateRange(from, to);
                } else {
                    dateRange = dateRangeTransformer.transform(message);
                }

                Points.SparseVector sparseVector = sparseVectorService.embed(transformedMessage);
                Points.DenseVector denseVector = denseVectorService.embed(transformedMessage);

                FilterConditionBuilder filterConditionBuilder = FilterConditionBuilder.newBuilder();

                if (dateRange != null && (dateRange.startDate != null || dateRange.endDate != null)) {
                    filterConditionBuilder.setDateRange(dateRange);
                    emitter.send(SseEmitter.event().name("dateRange").data(dateRange));
                }
                List<Common.Condition> filterConditions = filterConditionBuilder.build();

                List<Points.ScoredPoint> scoredPoints = this.client.queryAsync(

                        QueryPoints.newBuilder()
                                .setCollectionName(vectorStoreClientConfig.getCollectionName())
                                .setLimit(100)
                                .setScoreThreshold(0.25f)
                                .addPrefetch(Points.PrefetchQuery.newBuilder()
                                        .setQuery(nearest(sparseVector.getValuesList(), sparseVector.getIndicesList()))
                                        .setUsing("sparse")
                                        .setLimit(100)
                                        .setScoreThreshold(0.5f)
                                        .build())
                                .addPrefetch(Points.PrefetchQuery.newBuilder()
                                        .setQuery(nearest(denseVector.getDataList()))
                                        .setUsing("dense")
                                        .setScoreThreshold(0.75f)
                                        .setLimit(100)
                                        .build())
                                .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                                .setQuery(fusion(Points.Fusion.RRF))

/*
                        .setQuery(
                                formula(
                                        Formula.newBuilder()
                                                .setExpression(
                                                        sum( //  the final score = score + exp_decay(target_time - x_time)
                                                                SumExpression.newBuilder()
                                                                        .addSum(variable("$score"))
                                                                        .addSum(
                                                                                expDecay(
                                                                                        DecayParamsExpression.newBuilder()
                                                                                                .setX(
                                                                                                        datetimeKey("source_date_time"))  // payload key
                                                                                                .setTarget(
                                                                                                        datetime("YYYY-MM-DDT00:00:00Z"))  // current datetime
                                                                                                .setMidpoint(0.5f)
                                                                                                .setScale(86400)  // 1 day in seconds
                                                                                                .build()))
                                                                        .build()))
                                                .build()))*/


                                .setFilter(
                                        Common.Filter.newBuilder()
                                                .addAllMust(filterConditions)
                                                .build())
                                .build()


                ).get();
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

                String answer = "";
                if (documents.size() > 0) {
                    answer = chatClient.prompt()
                            .system(promptTemplateSpec -> promptTemplateSpec
                                    .text(systemPromptTemplate)
                                    .param("documents", documentContext.toString())
                            )
                            .user(message)
                            .call()
                            .content();

                    logger.info("Generated answer: {}", answer);
                }
                emitter.send(SseEmitter.event().name("answer").data(answer));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

    @GetMapping("hybridquery-stream")
    public SseEmitter hybridqueryStream(
            @RequestParam("q") String message,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to
    ) throws Exception {
        logger.info("Received hybridquery request with message: {}", message);
        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                String transformedMessage = compressionTransformer.transform(message);
                logger.info("Transformed message: {}", transformedMessage);

                DateRange dateRange;
                if (from != null || to != null) {
                    dateRange = new DateRange(from, to);
                } else {
                    dateRange = dateRangeTransformer.transform(message);
                }

                Points.SparseVector sparseVector = sparseVectorService.embed(transformedMessage);
                Points.DenseVector denseVector = denseVectorService.embed(transformedMessage);

                FilterConditionBuilder filterConditionBuilder = FilterConditionBuilder.newBuilder();

                if (dateRange != null && (dateRange.startDate != null || dateRange.endDate != null)) {
                    filterConditionBuilder.setDateRange(dateRange);
                    emitter.send(SseEmitter.event().name("dateRange").data(dateRange));
                }
                List<Common.Condition> filterConditions = filterConditionBuilder.build();

                Points.QueryPoints queryPoints = queryService.buildQueryRankByDate(sparseVector, denseVector, filterConditions);

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

                String answer = "";
                if (documents.size() > 0) {
                    answer = chatClient.prompt()
                            .system(promptTemplateSpec -> promptTemplateSpec
                                    .text(systemPromptTemplate)
                                    .param("documents", documentContext.toString())
                            )
                            .user(message)
                            .call()
                            .content();

                    logger.info("Generated answer: {}", answer);
                }
                emitter.send(SseEmitter.event().name("answer").data(answer));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }
}

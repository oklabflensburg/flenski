package com.flenski.service;

import com.flenski.BoostQuerySumExpressionBuilder;
import com.flenski.QueryPointsBuilder;
import com.flenski.config.QueryConfig;
import com.flenski.dto.DocumentDto;
import com.flenski.dto.QueryParameterBag;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points;
import io.qdrant.client.grpc.Points.QueryPoints;
import java.util.List;
import com.flenski.config.VectorStoreClientConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class QueryService {

    private static final Logger logger = LoggerFactory.getLogger(QueryService.class);

    private VectorStoreClientConfig vectorStoreClientConfig;
    private SparseVectorService sparseVectorService;
    private DenseVectorService denseVectorService;

    public QueryService(
            VectorStoreClientConfig vectorStoreClientConfig,
            SparseVectorService sparseVectorService,
            DenseVectorService denseVectorService,
            ChatClient chatClient
    ) {
        this.vectorStoreClientConfig = vectorStoreClientConfig;
        this.sparseVectorService = sparseVectorService;
        this.denseVectorService = denseVectorService;
    }

    public List<DocumentDto> query(QdrantClient client, String message, QueryParameterBag queryParameterBag, QueryConfig queryConfig) throws Exception {

        BoostQuerySumExpressionBuilder.Builder sumExpressionBuilder = BoostQuerySumExpressionBuilder.newBuilder();

        if (queryParameterBag.getEnableTimeBoost()) {
            sumExpressionBuilder.setTimeBoost(queryParameterBag.getTimeBoostScale(), queryConfig.getTimeBoostMidpoint(), queryConfig.getTimeBoostDateField());
        }

        if (queryParameterBag.getEnableTitleBoost()) {
            sumExpressionBuilder.setTitleBoost(message, queryParameterBag.getTitleBoostFactor());
        }

        QueryPointsBuilder.Builder queryPointsBuilder = QueryPointsBuilder.newBuilder(queryParameterBag, queryConfig);
        if (queryParameterBag.getQueryMode() == QueryParameterBag.QueryMode.LEXICAL || queryParameterBag.getQueryMode() == QueryParameterBag.QueryMode.HYBRID) {
            Points.SparseVector sparseVector = sparseVectorService.embed(message);
            queryPointsBuilder.setSparsePrefetchQuery(sparseVector, queryParameterBag.getLimit());
        }

        if (queryParameterBag.getQueryMode() == QueryParameterBag.QueryMode.SEMANTIC || queryParameterBag.getQueryMode() == QueryParameterBag.QueryMode.HYBRID) {
            Points.DenseVector denseVector = denseVectorService.embed(message);
            queryPointsBuilder.setDensePrefetchQuery(denseVector, queryParameterBag.getLimit());
        }

        Points.Query expressionQuery = sumExpressionBuilder.build();
        QueryPoints queryPoints = queryPointsBuilder
                .setExpressionQuery(expressionQuery)
                .setFilterByCategories(queryParameterBag.getCategories())
                .build();

        List<Points.ScoredPoint> scoredPoints = client.queryAsync(queryPoints).get();
        scoredPoints = scoredPoints.stream().toList();
        return scoredPoints.stream().map(DocumentDto::fromScoredPoint).toList();
    }
}

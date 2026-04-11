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
        Points.SparseVector sparseVector = sparseVectorService.embed(message);

        BoostQuerySumExpressionBuilder.Builder sumExpressionBuilder = BoostQuerySumExpressionBuilder.newBuilder();

        if (queryParameterBag.getEnableTimeBoost()) {
            sumExpressionBuilder.setTimeBoost(queryParameterBag.getTimeBoostScale(), queryConfig.getTimeBoostMidpoint(), queryConfig.getTimeBoostDateField());
        }

        if (queryParameterBag.getEnableTitleBoost()) {
            sumExpressionBuilder.setTitleBoost(message, queryParameterBag.getTitleBoostFactor());
        }

        Points.Query expressionQuery = sumExpressionBuilder.build();

        QueryPoints queryPoints = QueryPointsBuilder.newBuilder(queryParameterBag, queryConfig)
                .setSparsePrefetchQuery(sparseVector, queryParameterBag.getLimit())
                .setExpressionQuery(expressionQuery)
                .setFilterByCategories(queryParameterBag.getCategories())
                .build();

        List<Points.ScoredPoint> scoredPoints = client.queryAsync(queryPoints).get();
        scoredPoints = scoredPoints.stream().toList();
        return scoredPoints.stream().map(DocumentDto::fromScoredPoint).toList();
    }
/*
    public QueryPoints buildQuery(Points.SparseVector sparseVector, Points.DenseVector denseVector, List<Common.Condition> filterConditions) {
        return QueryPoints.newBuilder()
                .setCollectionName(vectorStoreClientConfig.getCollectionName())
                .setLimit(100)
                .setScoreThreshold(2f)
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
                .setFilter(
                        Common.Filter.newBuilder()
                                .addAllMust(filterConditions)
                                .build())
                .build();
    }
*/
}

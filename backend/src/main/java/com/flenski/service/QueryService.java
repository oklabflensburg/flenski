package com.flenski.service;

import com.flenski.PreConfiguredQueryBuilder;
import com.flenski.config.IndexingConfig;
import com.flenski.config.QueryConfig;
import com.flenski.dto.DocumentDto;
import com.flenski.dto.QueryParameterBag;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Common;
import io.qdrant.client.grpc.Points;
import io.qdrant.client.grpc.Points.QueryPoints;
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

import java.time.Instant;
import java.util.List;

import static io.qdrant.client.QueryFactory.fusion;

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

        QueryPoints queryPoints = new PreConfiguredQueryBuilder.Builder(vectorStoreClientConfig, queryParameterBag, queryConfig)
                .sparseQuery(sparseVector, IndexingConfig.sparseVectorName)
                .timeBoostQuery(queryParameterBag.getTimeBoostScale(), queryConfig.getTimeBoostMidpoint(), queryConfig.getTimeBoostDateField())
                .build()
                .build();

        List<Points.ScoredPoint> scoredPoints = client.queryAsync(queryPoints).get();
        scoredPoints = scoredPoints.stream().toList();
        return scoredPoints.stream().map(DocumentDto::fromScoredPoint).toList();
    }

    private QueryPoints.Builder getQueryPointsBuilder(QueryParameterBag parameterBag) {
        return QueryPoints.newBuilder()
                .setCollectionName(vectorStoreClientConfig.getCollectionName())
                .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                .setLimit(parameterBag.getLimit());
    }

    private QueryPoints.Builder setSparseQuery(QueryPoints.Builder builder, Points.SparseVector sparseVector) {
        return builder
                .setQuery(buildNearesSparseQuery(sparseVector))
                .setUsing(IndexingConfig.sparseVectorName);

    }

    private Points.Query buildNearesSparseQuery(Points.SparseVector sparseVector) {
        return nearest(sparseVector.getValuesList(), sparseVector.getIndicesList());
    }



    public Points.QueryPoints buildSparseQuery(Points.SparseVector sparseVector) {
        return QueryPoints.newBuilder()
                .setCollectionName(vectorStoreClientConfig.getCollectionName())
                .setLimit(100)
                .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                .setQuery(nearest(sparseVector.getValuesList(), sparseVector.getIndicesList()))
                .setUsing("sparse")
                .build();
    }

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

    public Points.QueryPoints buildQueryRankByDate(Points.SparseVector sparseVector, Points.DenseVector denseVector, List<Common.Condition> filterConditions) {
        return Points.QueryPoints.newBuilder()
                .setCollectionName(vectorStoreClientConfig.getCollectionName())
                .setLimit(100)
                //.setScoreThreshold(3f)
                .addPrefetch(Points.PrefetchQuery.newBuilder()
                        .setQuery(nearest(sparseVector.getValuesList(), sparseVector.getIndicesList()))
                        .setUsing("sparse")
                        .setLimit(100)
                        //      .setScoreThreshold(3f)
                        .build())
                /*
                .addPrefetch(Points.PrefetchQuery.newBuilder()
                        .setQuery(nearest(denseVector.getDataList()))
                        .setUsing("dense")
                        .setScoreThreshold(3f)
                        .setLimit(100)
                        .build())*/
                .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
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
                                                                                                datetime(Instant.now().toString()))  // current datetime
                                                                                        .setMidpoint(0.5f)
                                                                                        .setScale(86400)  // 1 day in seconds
                                                                                        .build()))
                                                                .build()))
                                        .build()))

                .setFilter(
                        Common.Filter.newBuilder()
                                .addAllMust(filterConditions)
                                .build())
                .build();
    }
}

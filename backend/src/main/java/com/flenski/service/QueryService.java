package com.flenski.service;

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
import static io.qdrant.client.QueryFactory.nearest;

import com.flenski.config.VectorStoreClientConfig;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class QueryService {

    VectorStoreClientConfig vectorStoreClientConfig;
    SparseVectorService sparseVectorService;
    DenseVectorService denseVectorService;

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

    public QueryPoints buildQuery(Points.SparseVector sparseVector, Points.DenseVector denseVector, List<Common.Condition> filterConditions) {
        return QueryPoints.newBuilder()
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
                .setScoreThreshold(0.1f)
                .addPrefetch(Points.PrefetchQuery.newBuilder()
                        .setQuery(nearest(sparseVector.getValuesList(), sparseVector.getIndicesList()))
                        .setUsing("sparse")
                        .setLimit(100)
                        .setScoreThreshold(0.1f)
                        .build())
                .addPrefetch(Points.PrefetchQuery.newBuilder()
                        .setQuery(nearest(denseVector.getDataList()))
                        .setUsing("dense")
                        .setScoreThreshold(0.1f)
                        .setLimit(100)
                        .build())
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

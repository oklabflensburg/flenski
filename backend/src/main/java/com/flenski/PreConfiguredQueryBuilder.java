package com.flenski;

import com.flenski.config.IndexingConfig;
import com.flenski.config.QueryConfig;
import com.flenski.config.VectorStoreClientConfig;
import com.flenski.dto.QueryParameterBag;
import io.qdrant.client.grpc.Points;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static io.qdrant.client.ExpressionFactory.*;
import static io.qdrant.client.ExpressionFactory.datetime;
import static io.qdrant.client.ExpressionFactory.datetimeKey;
import static io.qdrant.client.QueryFactory.formula;
import static io.qdrant.client.QueryFactory.nearest;

/**
 * A Wrapper for QueryPoints.Builder. Sets the config for the query and builds the underlying Builder.
 */
public class PreConfiguredQueryBuilder {

    private Points.QueryPoints.Builder queryPointsBuilder;
    private Builder builder;
    private QueryConfig queryConfig;
    private static final Logger logger = LoggerFactory.getLogger(PreConfiguredQueryBuilder.class);


    private enum QueryType {
        SPARSE, SPARSE_WITH_TIMEBOOST
    }

    public PreConfiguredQueryBuilder(Builder builder) {
        this.builder = builder;
        this.queryConfig = builder.queryConfig;
        this.init();
    }

    private void init() {
        this.initQueryPointsBuilder();

        QueryType queryType = getQueryType();
        logger.info("QueryType {} ", queryType);

        switch (queryType) {
            case SPARSE -> initSparseQueryBasic();
            case SPARSE_WITH_TIMEBOOST -> initSparseQueryTimeBoosted();
        }
    }

    private QueryType getQueryType() {
        if (builder.queryParameterBag.getQueryMode() == QueryParameterBag.QueryMode.LEXICAL) {
            if (builder.queryParameterBag.getEnableTimeBoost()) {
                return QueryType.SPARSE_WITH_TIMEBOOST;
            }
            return QueryType.SPARSE;
        }
        return QueryType.SPARSE;
    }

    private void initQueryPointsBuilder() {

        String collectionName = queryConfig.getCollection(builder.queryParameterBag.getCollection());
        this.queryPointsBuilder = Points.QueryPoints.newBuilder()
                .setCollectionName(collectionName)
                .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                .setLimit(builder.queryParameterBag.getLimit());
    }

    private void initSparseQueryBasic() {
        this.queryPointsBuilder.setQuery(builder.nearestSparseQuery).setUsing(builder.using);
    }

    private void initSparseQueryTimeBoosted() {
        this.queryPointsBuilder.addPrefetch(Points.PrefetchQuery.newBuilder().setQuery(builder.nearestSparseQuery).setUsing(IndexingConfig.sparseVectorName).setLimit(builder.queryParameterBag.getLimit()).build());
        this.queryPointsBuilder.setQuery(builder.timeBoostQuery);
    }

    public Points.QueryPoints build() {
        return this.queryPointsBuilder.build();
    }

    public static class Builder {

        private Points.Query nearestSparseQuery;
        private VectorStoreClientConfig vectorStoreClientConfig;
        private QueryParameterBag queryParameterBag;
        private String using;
        private Points.Query timeBoostQuery;
        private QueryConfig queryConfig;


        public Builder(
                VectorStoreClientConfig vectorStoreClientConfig,
                QueryParameterBag queryParameterBag,
                QueryConfig queryConfig) {
            this.vectorStoreClientConfig = vectorStoreClientConfig;
            this.queryParameterBag = queryParameterBag;
            this.queryConfig = queryConfig;
        }

        public Builder sparseQuery(Points.SparseVector sparseVector, String sparseVectorName) {
            this.nearestSparseQuery(sparseVector);
            this.using = sparseVectorName;
            return this;
        }

        public Builder timeBoostQuery(int scale, float midPoint, String dateTimeField) {
            this.timeBoostQuery = formula(
                    Points.Formula.newBuilder()
                            .setExpression(
                                    sum( //  the final score = score + exp_decay(target_time - x_time)
                                            Points.SumExpression.newBuilder()
                                                    .addSum(variable("$score"))
                                                    .addSum(
                                                            expDecay(
                                                                    Points.DecayParamsExpression.newBuilder()
                                                                            .setX(
                                                                                    datetimeKey(dateTimeField))  // payload key
                                                                            .setTarget(
                                                                                    datetime(Instant.now().toString()))  // current datetime
                                                                            .setMidpoint(midPoint)
                                                                            .setScale(scale * 86400)
                                                                            .build()))
                                                    .build()))
                            .build());
            return this;
        }

        public Builder nearestSparseQuery(Points.SparseVector sparseVector) {
            this.nearestSparseQuery = nearest(sparseVector.getValuesList(), sparseVector.getIndicesList());
            return this;
        }

        public Builder using(String sparseVectorName) {
            this.using = IndexingConfig.sparseVectorName;
            return this;
        }

        public PreConfiguredQueryBuilder build() {
            return new PreConfiguredQueryBuilder(this);
        }
    }
}

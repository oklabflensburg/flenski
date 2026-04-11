package com.flenski;

import com.flenski.config.IndexingConfig;
import com.flenski.config.QueryConfig;
import com.flenski.dto.QueryParameterBag;
import io.qdrant.client.grpc.Common;
import io.qdrant.client.grpc.Points;
import static io.qdrant.client.ConditionFactory.matchKeyword;
import java.util.List;

import static io.qdrant.client.QueryFactory.nearest;

public class QueryPointsBuilder {

    public static Builder newBuilder(QueryParameterBag queryParameterBag, QueryConfig queryConfig) {
        return new Builder(queryParameterBag, queryConfig);
    }

    record TimeBoostParameters(int scale, float midPoint, String dateTimeField) {
    }

    public static class Builder {

        QueryParameterBag queryParameterBag;
        Points.PrefetchQuery sparsePrefetchQuery;
        TimeBoostParameters timeBoostParameters;
        QueryConfig queryConfig;
        Points.Query expressionQuery;
        Common.Filter categoriesFilter;

        public Builder(QueryParameterBag queryParameterBag, QueryConfig queryConfig) {
            this.queryParameterBag = queryParameterBag;
            this.queryConfig = queryConfig;
        }

        public Builder setFilterByCategories(List<String> categories) {
            if (categories != null && !categories.isEmpty()) {
                List<Common.Condition> conditions = categories.stream()
                        .map(category -> matchKeyword("categories", category))
                        .toList();
                this.categoriesFilter = Common.Filter.newBuilder()
                        .addAllShould(conditions)
                        .build();
            }
            return this;
        }

        public Builder setSparsePrefetchQuery(Points.SparseVector sparseVector, int limit) {
            this.sparsePrefetchQuery = Points.PrefetchQuery.newBuilder()
                    .setQuery(nearest(sparseVector.getValuesList(), sparseVector.getIndicesList()))
                    .setUsing(IndexingConfig.sparseVectorName)
                    .setLimit(limit)
                    .build();
            return this;
        }

        public Builder setExpressionQuery(Points.Query query) {
            this.expressionQuery = query;
            return this;
        }

        public Points.QueryPoints build() {
            Points.QueryPoints.Builder builder = Points.QueryPoints.newBuilder()
                    .setCollectionName(queryConfig.getCollection(queryParameterBag.getCollection()))
                    .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                    .setFilter(this.categoriesFilter != null
                            ? this.categoriesFilter
                            : Common.Filter.newBuilder().build())
                    .setLimit(queryParameterBag.getLimit());

            if (this.sparsePrefetchQuery != null) {
                builder.addPrefetch(this.sparsePrefetchQuery);
            }

            if (this.expressionQuery != null) {
                builder.setQuery(this.expressionQuery);
            }
            return builder.build();
        }
    }
}

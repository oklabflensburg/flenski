package com.flenski;

import io.qdrant.client.grpc.Points;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static io.qdrant.client.ConditionFactory.matchText;
import static io.qdrant.client.ExpressionFactory.*;
import static io.qdrant.client.QueryFactory.formula;

public class BoostQuerySumExpressionBuilder {

     public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private static final Logger logger = LoggerFactory.getLogger(Builder.class);

        Points.Expression timeBoostExpression;
        Points.Expression titleBoostExpression;

        /**
         * Adds an exponential-decay time boost.
         *
         * @param scaleDays     half-life in days (must be positive)
         * @param midPoint      decay midpoint
         * @param dateTimeField payload key containing the datetime value
         */
        public Builder setTimeBoost(int scaleDays, float midPoint, String dateTimeField) {
            if (scaleDays <= 0) throw new IllegalArgumentException("scaleDays must be positive");
            if (dateTimeField == null || dateTimeField.isBlank())
                throw new IllegalArgumentException("dateTimeField must not be blank");

            logger.info("Set TimeBoost. MidpPoint: {}, scaleDays: {} ", midPoint, scaleDays);
            this.timeBoostExpression = expDecay(
                    Points.DecayParamsExpression.newBuilder()
                            .setX(datetimeKey(dateTimeField))
                            .setTarget(datetime(Instant.now().toString()))
                            .setMidpoint(midPoint)
                            .setScale((long) scaleDays * 86400)  // days → seconds, long to avoid overflow
                            .build()
            );
            return this;
        }

        /**
         * Adds a title-match boost.
         *
         * @param query search query to match against the title field
         * @param boost multiplier applied when the title matches
         */
        public Builder setTitleBoost(String query, float boost) {
            if (query == null || query.isBlank()) throw new IllegalArgumentException("query must not be blank");

            logger.info("Set titleBoost. Boost: {}", boost);
            this.titleBoostExpression = mult(
                    Points.MultExpression.newBuilder()
                            .addMult(constant(boost))
                            .addMult(condition(matchText("title", query)))
                            .build()
            );
            return this;
        }

        public Points.Query build() {

            Points.SumExpression.Builder sumBuilder = Points.SumExpression.newBuilder()
                    .addSum(variable("$score"));

            if (timeBoostExpression != null) {
                sumBuilder.addSum(timeBoostExpression);
            }
            if (titleBoostExpression != null) {
                sumBuilder.addSum(titleBoostExpression);
            }

            return formula(
                    Points.Formula.newBuilder()
                            .setExpression(sum(sumBuilder.build()))
                            .build()
            );
        }
    }
}

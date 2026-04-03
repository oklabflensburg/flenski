package com.flenski.config;

import com.flenski.dto.QueryParameterBag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QueryConfig {

    @Value("${flenski.query.parameters.queryMode}")
    private QueryParameterBag.QueryMode queryMode;

    @Value("${flenski.query.parameters.timeBoost.enable}")
    private Boolean enableTimeBoost;

    @Value("${flenski.query.parameters.timeBoost.midPoint}")
    private float timeBoostMidpoint;

    @Value("${flenski.query.parameters.timeBoost.scale}")
    private int timeBoostScale;

    @Value("${flenski.query.parameters.timeBoost.dateField}")
    private String timeBoostDateField;

    @Value("${flenski.query.parameters.limit}")
    private int limit;

    @Value("${flenski.query.parameters.maxLimit}")
    private int maxLimit;

    @Value("${flenski.query.parameters.collection}")
    private String collection;

    @Value("${spring.ai.vectorstore.qdrant.collection}")
    private String collectionName;

    @Value("${spring.ai.vectorstore.qdrant.collection-test}")
    private String testCollectionName;

    public QueryParameterBag.QueryMode getQueryMode() {
        return queryMode;
    }

    public Boolean getEnableTimeBoost() {
        return enableTimeBoost;
    }

    public float getTimeBoostMidpoint() {
        return timeBoostMidpoint;
    }

    public int getTimeBoostScale() {
        return timeBoostScale;
    }

    public String getTimeBoostDateField() { return timeBoostDateField; }

    public int getLimit() {
        if (limit > getMaxLimit()) {
            return getMaxLimit();
        }
        return limit;
    }

    public int getMaxLimit() {
        return maxLimit;
    }

    public String getCollection(String mode) {
        return mode.equals("production") ? collectionName : testCollectionName;
    }
}

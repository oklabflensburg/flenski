package com.flenski.dto;

import java.time.Instant;
import com.flenski.config.QueryConfig;
import lombok.ToString;

@ToString
public class QueryParameterBag
{
    private Instant fromSourceDateTime = null;

    private Instant untilSourceDateTime = null;

    private QueryMode queryMode;

    private Boolean enableTimeBoost = null;

    private float timeBoostMidPoint = 0f;

    private int timeBoostScale = 0;

    private int limit = 0;

    private String timeBoostDateField;

    public enum QueryMode
    {
        LEXICAL,
        SEMANTIC,
        HYBRID
    }

    public void initFromConfig(QueryConfig queryConfig) {
        if (this.queryMode == null) {
            this.queryMode = queryConfig.getQueryMode();
        }
        if (this.enableTimeBoost == null) {
            this.enableTimeBoost = queryConfig.getEnableTimeBoost();
        }

        if (this.limit == 0 || this.limit > queryConfig.getMaxLimit()) {
            this.limit = queryConfig.getLimit();
        }

        if (this.timeBoostMidPoint == 0f) {
            this.timeBoostMidPoint = queryConfig.getTimeBoostMidpoint();
        }

        if (this.timeBoostScale == 0) {
            this.timeBoostScale = queryConfig.getTimeBoostScale();
        }

        if (this.timeBoostDateField == null) {
            this.timeBoostDateField = queryConfig.getTimeBoostDateField();
        }
    }

    public Instant getFromSourceDateTime() { return fromSourceDateTime; }

    public Instant getUntilSourceDateTime() { return untilSourceDateTime; }

    public QueryMode getQueryMode() { return queryMode; }

    public int getLimit() { return limit; }

    public boolean getEnableTimeBoost() { return enableTimeBoost; }

    public float getTimeBoostMidPoint() { return timeBoostMidPoint; }

    public int getTimeBoostScale() { return timeBoostScale; }

    public String getTimeBoostDateField() { return timeBoostDateField; }

    public void setTimeBoostDateField(String timeBoostDateField) { this.timeBoostDateField = timeBoostDateField; }

    public void setFromSourceDateTime(Instant fromSourceDateTime) { this.fromSourceDateTime = fromSourceDateTime; }

    public void setUntilSourceDateTime(Instant untilSourceDateTime) { this.untilSourceDateTime = untilSourceDateTime; }

    public void setQueryMode(QueryMode queryMode) { this.queryMode = queryMode; }

    public void setEnableTimeBoost(Boolean enableTimeBoost) { this.enableTimeBoost = enableTimeBoost; }

    public void setTimeBoostMidPoint(float timeBoostMidPoint) { this.timeBoostMidPoint = timeBoostMidPoint; }

    public void setTimeBoostScale(int timeBoostScale) { this.timeBoostScale = timeBoostScale; }

    public void setLimit(int limit) { this.limit = limit; }

}

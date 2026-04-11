package com.flenski.dto;

import java.time.Instant;
import java.util.List;

import com.flenski.config.QueryConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class QueryParameterBag
{
    private Instant fromSourceDateTime = null;

    private Instant untilSourceDateTime = null;

    private QueryMode queryMode;

    private Boolean enableTimeBoost = null;

    private int timeBoostScale = 0;

    private Boolean enableTitleBoost = null;

    private float titleBoostFactor = 0;

    private int limit = 0;

    private String collection;

    private List<String> categories;

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

        if (this.timeBoostScale == 0) {
            this.timeBoostScale = queryConfig.getTimeBoostScale();
        }

        if (this.enableTitleBoost == null) {
            this.enableTitleBoost = queryConfig.getEnableTitleBoost();
        }

        if (this.titleBoostFactor == 0) {
            this.titleBoostFactor = queryConfig.getTitleBoostFactor();
        }
    }
}

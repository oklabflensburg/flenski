package com.flenski;

import com.flenski.value.DateRange;
import com.google.protobuf.Timestamp;
import io.qdrant.client.grpc.Common;
import java.time.Instant;
import java.util.List;

import static io.qdrant.client.ConditionFactory.datetimeRange;

public class FilterConditionBuilder {

    private DateRange dateRange;
    private List<Common.Condition> filterConditions;

    FilterConditionBuilder() {
        this.filterConditions = new java.util.ArrayList<>();
    }

    public static FilterConditionBuilder newBuilder() {
        return new FilterConditionBuilder();
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public List<Common.Condition>  build() throws Exception {
        if (dateRange != null && (dateRange.startDate != null || dateRange.endDate != null)) {
            Instant startInstant = null;
            Instant endInstant = null;
            if (dateRange.startDate != null) {
                String start = dateRange.startDate.length() == 10 ? dateRange.startDate + "T00:00:00Z" : dateRange.startDate;
                startInstant = Instant.parse(start);
            }
            if (dateRange.endDate != null) {
                String end = dateRange.endDate.length() == 10 ? dateRange.endDate + "T23:59:59Z" : dateRange.endDate;
                endInstant = Instant.parse(end);
            }

            if (startInstant != null || endInstant != null) {
                Common.DatetimeRange.Builder rangeBuilder = Common.DatetimeRange.newBuilder();
                if (startInstant != null) {
                    rangeBuilder.setGt(Timestamp.newBuilder().setSeconds(startInstant.getEpochSecond()));
                }
                if (endInstant != null) {
                    rangeBuilder.setLte(Timestamp.newBuilder().setSeconds(endInstant.getEpochSecond()));
                }
                Common.Condition dateTimeRangeCondition = datetimeRange("source_date_time", rangeBuilder.build());
                filterConditions.add(dateTimeRangeCondition);
            }
        }
        return filterConditions;
    }
}

package io.insave.marketdata.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AggregateBarsResponse {

    private String ticker;
    private int queryCount;
    private int resultsCount;
    private Boolean adjusted;
    private List<AggregateBar> results;
    private String status;

    @JsonProperty("request_id")
    private String requestId;

    private int count;

    @Data
    public static class AggregateBar {
        @JsonProperty("o")
        private BigDecimal open;

        @JsonProperty("c")
        private BigDecimal close;

        @JsonProperty("v")
        private BigDecimal volume;

        @JsonProperty("h")
        private BigDecimal high;

        @JsonProperty("l")
        private BigDecimal low;

        @JsonProperty("t")
        private Long date;
    }
}

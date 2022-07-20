package io.insave.marketdata.websocket.entity.polygon;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecondAggregate {

    @JsonAlias("ev")
    private String eventType;

    @JsonAlias("sym")
    private String ticker;

    @JsonAlias("v")
    private Long volume;

    @JsonAlias("av")
    private Long accumVolume;

    @JsonAlias("op")
    private BigDecimal open;

    @JsonAlias("vw")
    private BigDecimal weightedVolume;

    @JsonAlias("o")
    private BigDecimal openingTickPrice;

    @JsonAlias("c")
    private BigDecimal closingTickPrice;

    @JsonAlias("h")
    private BigDecimal high;

    @JsonAlias("l")
    private BigDecimal low;

    @JsonAlias("a")
    private BigDecimal todayWeightedVolume;

    @JsonAlias("z")
    private Long averageTradeSize;

    @JsonAlias("s")
    private Long start;

    @JsonAlias("e")
    private Long end;

}

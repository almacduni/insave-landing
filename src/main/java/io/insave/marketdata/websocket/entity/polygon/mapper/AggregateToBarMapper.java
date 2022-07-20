package io.insave.marketdata.websocket.entity.polygon.mapper;

import io.insave.marketdata.entity.Bar;
import io.insave.marketdata.websocket.entity.polygon.SecondAggregate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * class is needed for converting {@link SecondAggregate}
 * to {@link Bar}
 */
public class AggregateToBarMapper {

    public static List<Bar> convertToBars(List<SecondAggregate> aggregates) {
        return aggregates.stream()
                .map(aggregate ->
                        new Bar(aggregate.getOpeningTickPrice(),
                                aggregate.getClosingTickPrice(),
                                aggregate.getHigh(),
                                aggregate.getLow(),
                                aggregate.getStart(),
                                aggregate.getTicker())
                ).collect(Collectors.toList());
    }
}

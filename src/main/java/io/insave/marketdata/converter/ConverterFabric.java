package io.insave.marketdata.converter;


import io.insave.marketdata.entity.Bar;
import io.insave.marketdata.entity.TimeFrame;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

import static io.insave.marketdata.entity.TimeFrame.FIVE_MINUTES;
import static io.insave.marketdata.entity.TimeFrame.FOUR_HOUR;
import static io.insave.marketdata.entity.TimeFrame.ONE_HOUR;
import static io.insave.marketdata.entity.TimeFrame.THIRTY_MINUTES;

@Slf4j
@Component
@AllArgsConstructor
public class ConverterFabric {

    private Map<TimeFrame, MarketDataConverter> converters;

    @PostConstruct
    private void init() {
        converters.put(FIVE_MINUTES, new FiveMinutesConverter());
        converters.put(THIRTY_MINUTES, new ThirtyMinutesConverter());
        converters.put(ONE_HOUR, new OneHourConverter());
        converters.put(FOUR_HOUR, new FourHourConverter());
    }

    public List<Bar> convertToTimeFrame(List<Bar> barList, TimeFrame timeFrame) {
        MarketDataConverter converter = converters.get(timeFrame);
        return converter.convert(barList);
    }
}

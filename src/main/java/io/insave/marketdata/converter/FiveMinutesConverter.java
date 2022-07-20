package io.insave.marketdata.converter;

import io.insave.marketdata.entity.Bar;

import java.util.List;

public class FiveMinutesConverter extends MarketDataConverter {

    @Override
    public List<Bar> convert(List<Bar> bars) {
        return convertInTimeFrame(getTimeInterval(), getIndex(), bars);
    }

    @Override
    protected long getTimeInterval() {
        return 5L * 60L * 1000;
    }

    @Override
    protected int getIndex() {
        return 4;
    }
}

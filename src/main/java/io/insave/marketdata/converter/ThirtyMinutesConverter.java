package io.insave.marketdata.converter;

import io.insave.marketdata.entity.Bar;

import java.util.List;

public class ThirtyMinutesConverter extends MarketDataConverter {

    @Override
    public List<Bar> convert(List<Bar> bars) {
        return convertInTimeFrame(getTimeInterval(), getIndex(), bars);
    }

    @Override
    protected long getTimeInterval() {
        return 30L * 60L * 1000;
    }

    @Override
    protected int getIndex() {
        return 29;
    }
}


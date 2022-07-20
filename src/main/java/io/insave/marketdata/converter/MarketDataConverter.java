package io.insave.marketdata.converter;

import io.insave.marketdata.entity.Bar;
import io.insave.marketdata.entity.TimeFrame;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class MarketDataConverter {

    /**
     * creates list of {@link Bar} in provided {@link TimeFrame}
     * @param bars list of objects of {@link Bar} type
     * @return list of built list of {@link Bar}
     */
    public abstract List<Bar> convert(List<Bar> bars);

    protected final List<Bar> convertInTimeFrame(long timeInterval, int elementIndex, List<Bar> bars) {
        List<Bar> stockCandles = new ArrayList<>();
        long firstTime = bars.get(0).getDate();
        long offsetTime = firstTime % timeInterval;

        long startTime = firstTime - offsetTime;
        int index = elementIndex;
        long endTime = startTime + timeInterval;

        boolean passingAttempts = true;
        //Loop iterates over the original list, splits into intermediate lists depending on time frame.
        //If any bar fits in the time frame, it and the previous bars are used in the new bar.
        //Creating a new bar trims the original array.
        while (passingAttempts) {
            while (true) {
                Bar currentBar = bars.get(index);
                long barTime = currentBar.getDate();
                if (barTime < endTime) {
                    List<Bar> actualAggregates = bars.subList(0, index + 1);
                    long openTime = endTime - timeInterval;
                    setNewBar(stockCandles, actualAggregates, openTime);
                    bars = bars.subList(index + 1, bars.size());
                    endTime = endTime + timeInterval;
                    index = elementIndex;
                    break;
                } else {
                    index--;
                    if (index < 0) {
                        endTime = endTime + timeInterval;
                        index = elementIndex;
                        break;
                    }
                }
            }
            //Check for the last iterations over the original list. If bars are empty work is done
            if (bars.isEmpty()) {
                passingAttempts = false;
            }
            int barsSize = bars.size();
            if (index >= (barsSize - 1)) {
                index = barsSize - 1;
            }
        }

        return stockCandles;
    }

    protected abstract long getTimeInterval();

    protected abstract int getIndex();

    private void setNewBar(List<Bar> stockCandles, List<Bar> actualAggregates, long openTime) {
        stockCandles.add(new Bar(
                setOpen(actualAggregates),
                setClose(actualAggregates),
                setHigh(actualAggregates),
                setLow(actualAggregates),
                setTime(openTime)
        ));
    }

    /**
     * returns the open param that is represented in the last object of list
     * @param bars the list of {@link Bar}
     * @return the open value
     */
    protected final BigDecimal setOpen(List<Bar> bars) {
        return bars.stream()
                .findFirst()
                .get().getOpen();
    }

    /**
     * returns the close param that is represented in the last object of list
     * @param bars the list of {@link Bar}
     * @return the close value
     */
    protected final BigDecimal setClose(List<Bar> bars) {
        return bars.get(bars.size() - 1).getClose();//the last
    }

    /**
     * sets the object with max value of
     * {@code high} param on the first position of the list and returns this value
     * @param bars is the list of {@link Bar}
     * @return the High value
     */
    protected final BigDecimal setHigh(List<Bar> bars) {
        List<Bar> sortedList = new ArrayList<>(bars.size());
        sortedList.addAll(bars);
        sortedList.sort(highValueComparator);
        return sortedList.get(0).getHigh();
    }

    /**
     * sets the object with min value of
     * {@code low} param on the first position of the list and returns this value
     * @param bars the list of {@link Bar}
     * @return the High value
     */
    protected final BigDecimal setLow(List<Bar> bars) {
        List<Bar> sortedList = new ArrayList<>(bars.size());
        sortedList.addAll(bars);
        sortedList.sort(lowValueComparator.reversed());//reversed to sort in ascending order
        return sortedList.get(0).getLow();
    }

    /**
     * the time of the first object is the start time in current candle
     * @param unixTime the long type unix time
     * @return the time of the first object in {@link String} type
     */
    //TODO: time format
    protected final Long setTime(long unixTime) {
        return unixTime;
    }

    /**
     * Comparators to set the {@code high} and {@code low} values on the top
     * of sorted lists
     */
    private final Comparator<Bar> lowValueComparator =
            (e1, e2) -> e2.getLow().compareTo(e1.getLow());

    private final Comparator<Bar> highValueComparator =
            (e1, e2) -> e2.getHigh().compareTo(e1.getHigh());
}

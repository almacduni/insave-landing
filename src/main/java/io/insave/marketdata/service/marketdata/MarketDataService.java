package io.insave.marketdata.service.marketdata;

import com.google.common.collect.Lists;
import io.insave.marketdata.client.PolygonClient;
import io.insave.marketdata.entity.AggregateBarsResponse;
import io.insave.marketdata.entity.Bar;
import io.insave.marketdata.entity.TimeFrame;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class MarketDataService extends ActualMinuteBarCreator {

  private static final String COLLECTION_POSTFIX_ONE_MINUTE = "-STOCK-ONE-MINUTE";
  private static final String COLLECTION_POSTFIX_ONE_DAY = "-STOCK-ONE-DAY";
  public static final int SIZE_OF_THREAD_POOL = 2;

  // OPEN and CLOSE hours in GMT
  private static final long STOCK_MARKET_OPEN_TIME_IN_MILLIS =
      14 * HOUR_IN_MILLIS + 29 * MINUTE_IN_MILLIS;
  private static final long STOCK_MARKET_CLOSE_TIME_IN_MILLIS =
      20 * HOUR_IN_MILLIS + 59 * MINUTE_IN_MILLIS;

  private final PolygonClient polygonClient;
  private final MongoTemplate mongoTemplate;
  private final ExecutorService executorService = Executors.newFixedThreadPool(SIZE_OF_THREAD_POOL);

  private final List<String> polygonStockTickers =
      Lists.newArrayList("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA");

  @Autowired
  public MarketDataService(
      MongoTemplate mongoTemplate, PolygonClient polygonClient, MongoTemplate template) {
    super(mongoTemplate);
    this.polygonClient = polygonClient;
    this.mongoTemplate = template;
  }

  public void storeMarketData(String ticker, long daysAmount, TimeFrame timeFrame) {
    ZonedDateTime dateTimeNow = ZonedDateTime.now();
    long to = dateTimeNow.toInstant().toEpochMilli();
    long from = to - daysAmount * DAY_IN_MILLIS;

    // the limit of bars received from Polygon per request
    int barsAmount = 50000;
    List<Bar> barList = new ArrayList<>(barsAmount);

    while (barsAmount >= 50000) {
      AggregateBarsResponse tickerAggregates =
          polygonClient.getTickerAggregates(ticker, timeFrame, from, to);
      List<AggregateBarsResponse.AggregateBar> results = tickerAggregates.getResults();
      leaveBarsDuringTradingHours(timeFrame, barList, tickerAggregates);

      String collectionName = ticker + getCollectionPostfix(timeFrame);
      mongoTemplate.insert(barList, collectionName);
      barList.clear();
      int resultsCount = tickerAggregates.getResultsCount();
      if (resultsCount >= 50000) {
        to = results.get(resultsCount - 1).getDate();
      }
      barsAmount = resultsCount;
    }
  }

  public void createActualMinuteBarFromWebsocketUpdate(List<Bar> bars) {
    // filter bars by tickers and time in trading hours
    List<Bar> filteredBars =
        bars.stream()
            .filter(bar -> polygonStockTickers.contains(bar.getTicker()))
            .filter(
                bar -> {
                  long time = bar.getDate() % DAY_IN_MILLIS;
                  // is in trading hours
                  return (time > STOCK_MARKET_OPEN_TIME_IN_MILLIS
                      && time <= STOCK_MARKET_CLOSE_TIME_IN_MILLIS);
                })
            .collect(Collectors.toList());

    if (!filteredBars.isEmpty()) {
      Runnable processMinuteBar = () -> filteredBars.forEach(this::saveOrUpdateMinuteBar);
      Runnable processDayBar = () -> filteredBars.forEach(this::saveOrUpdateDayBar);
      executorService.execute(processDayBar);
      executorService.execute(processMinuteBar);
    }
  }

  private void saveOrUpdateMinuteBar(Bar bar) {
    saveOrUpdateBar(bar, bar.getTicker(), TimeFrame.ONE_MINUTE, MINUTE_IN_MILLIS);
  }

  private void saveOrUpdateDayBar(Bar bar) {
    saveOrUpdateBar(bar, bar.getTicker(), TimeFrame.ONE_DAY, DAY_IN_MILLIS);
  }

  @Override
  public String getCollectionPostfix(TimeFrame timeFrame) {
    if (TimeFrame.ONE_DAY.equals(timeFrame)) {
      return COLLECTION_POSTFIX_ONE_DAY;
    } else {
      return COLLECTION_POSTFIX_ONE_MINUTE;
    }
  }

  private void leaveBarsDuringTradingHours(
      TimeFrame timeFrame, List<Bar> barList, AggregateBarsResponse tickerAggregates) {
    for (AggregateBarsResponse.AggregateBar aggregateBar : tickerAggregates.getResults()) {
      // Reduce time to one day interval
      long time = aggregateBar.getDate() % DAY_IN_MILLIS;

      // Check if time is included in interval between OPEN and CLOSE hours
      if (timeFrame.equals(TimeFrame.ONE_DAY)
          || (time > STOCK_MARKET_OPEN_TIME_IN_MILLIS
              && time <= STOCK_MARKET_CLOSE_TIME_IN_MILLIS)) {
        Bar bar =
            new Bar(
                aggregateBar.getOpen(),
                aggregateBar.getClose(),
                aggregateBar.getHigh(),
                aggregateBar.getLow(),
                aggregateBar.getDate());

        barList.add(bar);
      }
    }
  }
}

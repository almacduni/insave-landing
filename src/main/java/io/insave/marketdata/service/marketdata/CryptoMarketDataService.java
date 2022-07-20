package io.insave.marketdata.service.marketdata;

import com.google.common.collect.Lists;
import io.insave.marketdata.client.BinanceClient;
import io.insave.marketdata.entity.Bar;
import io.insave.marketdata.entity.CryptoAsset;
import io.insave.marketdata.entity.Divisor;
import io.insave.marketdata.entity.TimeFrame;
import io.insave.marketdata.repository.CryptoAssetRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class CryptoMarketDataService extends ActualMinuteBarCreator {

	private static final long INDEX_INITIAL_PRICE = 100;
	private static final int BINANCE_MAX_ELEMENTS_PER_REQUEST = 1000;
	private static final String COLLECTION_POSTFIX_ONE_MINUTE = "-CRYPTO-ONE-MINUTE";
	private static final String COLLECTION_POSTFIX_ONE_DAY = "-CRYPTO-ONE-DAY";
	private static final String DIVISORS_COLLECTION_NAME = "DIVISORS";
	private static final String INDEX_ACTUAL_MARKET_CAPS_COLLECTION_NAME = "INDEX-ACTUAL-CRYPTO-MARKET-CAPS";
	private static final String INDEX_HISTORICAL_MARKET_CAPS_COLLECTION_NAME = "INDEX-HISTORICAL-CRYPTO-MARKET-CAPS";
	private static final String IN10_TICKER = "IN10USDT";

	private final MongoTemplate mongoTemplate;
	private final BinanceClient binanceClient;
	private final CryptoAssetRepository cryptoAssetRepository;
	private final List<String> binanceCryptoTickers = Lists.newArrayList("BTCUSDT", "ETHUSDT", "BNBUSDT", "ADAUSDT",
			"DOGEUSDT", "XRPUSDT", "DOTUSDT", "UNIUSDT", "LINKUSDT", "BCHUSDT");

	private BigDecimal divisor;

	@Autowired
	public CryptoMarketDataService(MongoTemplate mongoTemplate, BinanceClient binanceClient,
								   CryptoAssetRepository cryptoAssetRepository) {
		super(mongoTemplate);
		this.mongoTemplate = mongoTemplate;
		this.binanceClient = binanceClient;
		this.cryptoAssetRepository = cryptoAssetRepository;
		getDivisor();
	}

	@SneakyThrows
	public void storeIndexMarketData(long daysAmount, TimeFrame timeFrame) {
		long endTime = getEndTime(timeFrame);
		endTime = recalculateEndTime(timeFrame, endTime);
		// When endTime = startTime, we still will receive one bar. That's why need to subtract 1
		long startTime = endTime - (daysAmount - 1) * DAY_IN_MILLIS;
		int barsAmount = BINANCE_MAX_ELEMENTS_PER_REQUEST;
		List<Bar> barList = new ArrayList<>(BINANCE_MAX_ELEMENTS_PER_REQUEST);

		while (barsAmount == BINANCE_MAX_ELEMENTS_PER_REQUEST) {
			List<Bar> btcBars = getBars("BTCUSDT", timeFrame, startTime, endTime);
			List<Bar> ethBars = getBars("ETHUSDT", timeFrame, startTime, endTime);
			List<Bar> bnbBars = getBars("BNBUSDT", timeFrame, startTime, endTime);
			List<Bar> adaBars = getBars("ADAUSDT", timeFrame, startTime, endTime);
			List<Bar> dogeBars = getBars("DOGEUSDT", timeFrame, startTime, endTime);
			List<Bar> xrpBars = getBars("XRPUSDT", timeFrame, startTime, endTime);
			List<Bar> dotBars = getBars("DOTUSDT", timeFrame, startTime, endTime);
			List<Bar> uniBars = getBars("UNIUSDT", timeFrame, startTime, endTime);
			List<Bar> linkBars = getBars("LINKUSDT", timeFrame, startTime, endTime);
			List<Bar> bchBars = getBars("BCHUSDT", timeFrame, startTime, endTime);

			int resultsCount = btcBars.size();
			startTime = recalculateStartTime(timeFrame, btcBars.get(resultsCount - 1).getDate());
			barsAmount = resultsCount;

			//TODO Refactoring
			for (int i = 0; i < resultsCount; i++) {
				Bar btcBar = btcBars.get(i);

				List<Bar> barsToCalculate = new ArrayList<>();
				barsToCalculate.add(btcBar);
				barsToCalculate.add(ethBars.get(i));
				barsToCalculate.add(bnbBars.get(i));
				barsToCalculate.add(adaBars.get(i));
				barsToCalculate.add(dogeBars.get(i));
				barsToCalculate.add(xrpBars.get(i));
				barsToCalculate.add(dotBars.get(i));
				barsToCalculate.add(uniBars.get(i));
				barsToCalculate.add(linkBars.get(i));
				barsToCalculate.add(bchBars.get(i));

				long date = btcBar.getDate();

				List<Bar> filteredBars = barsToCalculate.stream()
						.filter(bar -> date == bar.getDate())
						.collect(Collectors.toList());

				List<Bar> barsWithMarketCap = getBarsWithMarketCap(filteredBars,
						INDEX_HISTORICAL_MARKET_CAPS_COLLECTION_NAME);
				if (barsWithMarketCap == null) {
					return;
				}

				Bar indexBar = calculateIndexBar(barsWithMarketCap);
				indexBar.setDate(date);
				indexBar.setTicker(IN10_TICKER);
				barList.add(indexBar);
			}

			String collectionName = IN10_TICKER + getCollectionPostfix(timeFrame);
			mongoTemplate.insert(barList, collectionName);
			barList.clear();
		}
	}

	public void storeCryptoMarketData(String ticker, long daysAmount, TimeFrame timeFrame) {
		ZonedDateTime dateTimeNow = ZonedDateTime.now();
		long endTime = dateTimeNow.toInstant().toEpochMilli();
		long startTime = endTime - daysAmount * DAY_IN_MILLIS;
		int barsAmount = BINANCE_MAX_ELEMENTS_PER_REQUEST;

		while (barsAmount == BINANCE_MAX_ELEMENTS_PER_REQUEST) {
			List<Bar> bars = getBars(ticker, timeFrame, startTime, endTime);
			int resultsCount = bars.size();

			startTime = recalculateStartTime(timeFrame, bars.get(resultsCount - 1).getDate());
			barsAmount = resultsCount;
			String collectionName = ticker + getCollectionPostfix(timeFrame);
			mongoTemplate.insert(bars, collectionName);
		}
	}

	public void createActualIndexBarFromWebsocketUpdate(List<Bar> bars) {
		List<Bar> filteredBars = bars.stream()
				.filter(bar -> binanceCryptoTickers.contains(bar.getTicker()))
				.collect(Collectors.toList());

		// We are operating with close price from websocket update
		filteredBars.forEach(bar -> {
			BigDecimal close = bar.getClose();
			bar.setOpen(close);
			bar.setHigh(close);
			bar.setLow(close);
		});

		List<Bar> barsWithMarketCap = getBarsWithMarketCap(filteredBars, INDEX_ACTUAL_MARKET_CAPS_COLLECTION_NAME);
		if (barsWithMarketCap == null) {
			return;
		}
		Bar bar = calculateIndexBar(barsWithMarketCap);

		saveOrUpdateBar(bar, IN10_TICKER, TimeFrame.ONE_MINUTE, MINUTE_IN_MILLIS);
		saveOrUpdateBar(bar, IN10_TICKER, TimeFrame.ONE_DAY, DAY_IN_MILLIS);
	}

	private List<Bar> getBarsWithMarketCap(List<Bar> barsWithUpdate, String collectionNameForMarketCaps) {
		List<Bar> barsWithMarketCap = calculateMarketCapForAllPrices(barsWithUpdate);
		List<String> tickersWithoutUpdate = getTickersWithoutUpdate(barsWithUpdate);

		if (!tickersWithoutUpdate.isEmpty()) {
			// If there are no updates for some assets, we take the previous market caps
			addMissingBars(barsWithMarketCap, tickersWithoutUpdate,
					collectionNameForMarketCaps);
		}

		int assetsAmount = 10;
		if (barsWithMarketCap.size() != assetsAmount) {
			return null;
		}

		mongoTemplate.dropCollection(collectionNameForMarketCaps);
		mongoTemplate.insert(barsWithMarketCap, collectionNameForMarketCaps);
		return barsWithMarketCap;
	}

	private void addMissingBars(List<Bar> barsWithMarketCap, List<String> tickersWithoutUpdate, String collectionName) {
		Query query = new Query();
		query.addCriteria(Criteria.where("ticker").in(tickersWithoutUpdate));
		List<Bar> oldBarsWithMarketCap = mongoTemplate.find(query, Bar.class,
				collectionName);
		barsWithMarketCap.addAll(oldBarsWithMarketCap);
	}

	private List<String> getTickersWithoutUpdate(List<Bar> barsWithUpdate) {
		List<String> tickersWithUpdate = barsWithUpdate.stream()
				.map(Bar::getTicker)
				.collect(Collectors.toList());
		return binanceCryptoTickers.stream()
				.filter(ticker -> !tickersWithUpdate.contains(ticker))
				.collect(Collectors.toList());
	}

	private List<Bar> getBars(String ticker, TimeFrame timeFrame, long startTime, long endTime) {
		List<List<Object>> watchlistItems = binanceClient.getBarsPerMinute(ticker, timeFrame, startTime, endTime);
		return watchlistItems.stream()
				.map(item -> {
					Bar bar = new Bar();
					bar.setDate((Long) item.get(0));
					bar.setOpen(new BigDecimal((String) item.get(1)));
					bar.setHigh(new BigDecimal((String) item.get(2)));
					bar.setLow(new BigDecimal((String) item.get(3)));
					bar.setClose(new BigDecimal((String) item.get(4)));
					bar.setTicker(ticker);
					return bar;
				})
				.collect(Collectors.toList());
	}

	private List<Bar> calculateMarketCapForAllPrices(List<Bar> bars) {
		return bars.stream()
				.peek(bar -> {
					CryptoAsset cryptoAsset = cryptoAssetRepository.findCryptoAssetByTicker(bar.getTicker());
					bar.setOpen(cryptoAsset.getCirculatingSupply().multiply(bar.getOpen()));
					bar.setClose(cryptoAsset.getCirculatingSupply().multiply(bar.getClose()));
					bar.setLow(cryptoAsset.getCirculatingSupply().multiply(bar.getLow()));
					bar.setHigh(cryptoAsset.getCirculatingSupply().multiply(bar.getHigh()));
				}).collect(Collectors.toList());
	}

	private Bar calculateIndexBar(List<Bar> bars) {
		BigDecimal close = calculatePrice(bars.stream().map(Bar::getClose));
		BigDecimal open = calculatePrice(bars.stream().map(Bar::getOpen));
		BigDecimal high = calculatePrice(bars.stream().map(Bar::getHigh));
		BigDecimal low = calculatePrice(bars.stream().map(Bar::getLow));
		long date = bars.get(0).getDate();

		Bar indexBar = new Bar();
		indexBar.setOpen(open);
		indexBar.setHigh(high);
		indexBar.setLow(low);
		indexBar.setClose(close);
		indexBar.setDate(date);
		return indexBar;
	}

	private BigDecimal calculatePrice(Stream<BigDecimal> bigDecimalStream) {
		BigDecimal totalMarketCap = bigDecimalStream
				.reduce(BigDecimal::add).get();
		// it used only once during the first application run
		if (divisor == null) {
			divisor = totalMarketCap.divide(BigDecimal.valueOf(INDEX_INITIAL_PRICE));
			mongoTemplate.save(new Divisor(divisor), DIVISORS_COLLECTION_NAME);
		}
		return totalMarketCap.divide(divisor, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
	}

	private long getEndTime(TimeFrame timeFrame) {
		Query query = new Query();
		query.with(Sort.by(Sort.Order.asc("date")));
		Bar bar = mongoTemplate.findOne(query, Bar.class, IN10_TICKER + getCollectionPostfix(timeFrame));
		return bar.getDate();
	}

	private void getDivisor() {
		Optional<Divisor> divisor = Optional.ofNullable(
				mongoTemplate.findOne(new Query(), Divisor.class, DIVISORS_COLLECTION_NAME));
		divisor.ifPresent(value -> this.divisor = value.getDivisor());
	}

	private long recalculateEndTime(TimeFrame timeFrame, long date) {
		// Subtract day or minute in order to prevent duplicated elements while receiving bars
		if (TimeFrame.ONE_DAY.equals(timeFrame)) {
			date = date - DAY_IN_MILLIS;
		} else {
			date = date - MINUTE_IN_MILLIS;
		}
		return date;
	}

	private long recalculateStartTime(TimeFrame timeFrame, long date) {
		// Add day or minute in order to prevent duplicated elements while receiving bars
		if (TimeFrame.ONE_DAY.equals(timeFrame)) {
			date = date + DAY_IN_MILLIS;
		} else {
			date = date + MINUTE_IN_MILLIS;
		}
		return date;
	}

	@Override
	public String getCollectionPostfix(TimeFrame timeFrame) {
		if (TimeFrame.ONE_DAY.equals(timeFrame)) {
			return COLLECTION_POSTFIX_ONE_DAY;
		} else {
			return COLLECTION_POSTFIX_ONE_MINUTE;
		}
	}
}
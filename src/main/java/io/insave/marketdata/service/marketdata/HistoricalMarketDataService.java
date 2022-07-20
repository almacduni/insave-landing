package io.insave.marketdata.service.marketdata;

import com.google.common.collect.Lists;
import io.insave.marketdata.converter.ConverterFabric;
import io.insave.marketdata.entity.Bar;
import io.insave.marketdata.entity.TimeFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoricalMarketDataService {

	private static final String COLLECTION_POSTFIX_STOCK_ONE_MINUTE = "-STOCK-ONE-MINUTE";
	private static final String COLLECTION_POSTFIX_STOCK_ONE_DAY = "-STOCK-ONE-DAY";
	private static final String COLLECTION_POSTFIX_CRYPTO_ONE_MINUTE = "-CRYPTO-ONE-MINUTE";
	private static final String COLLECTION_POSTFIX_CRYPTO_ONE_DAY = "-CRYPTO-ONE-DAY";

	private final MongoTemplate mongoTemplate;
	private final ConverterFabric converterFabric;
	private final List<String> polygonStockTickers = Lists.newArrayList("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA");
	private final Integer barLimit = 200;

	// TODO receive market data with day timeframe
	public List<Bar> getMarketData(TimeFrame timeFrame, String ticker, Long fromDate, Long toDate) {
		String collectionName = getCollectionName(ticker);
		List<Bar> barList;
		if (fromDate == null) {
			barList = getLastBarsInTimeFrame(timeFrame, collectionName, toDate);
			// TODO change the logic of converting bars
			Collections.reverse(barList);
			List<Bar> convertedBars = converterFabric.convertToTimeFrame(barList, timeFrame);
			Collections.reverse(convertedBars);
			return convertedBars.stream()
					.limit(barLimit)
					.collect(Collectors.toList());
		} else {
			Query query = new Query();
			query.addCriteria(Criteria.where("date").lte(toDate).gte(fromDate));
			barList = mongoTemplate.find(query, Bar.class, collectionName);
			Collections.reverse(barList);
			return converterFabric.convertToTimeFrame(barList, timeFrame);
		}
	}

	// TODO add day collections
	private String getCollectionName(String ticker) {
		if (polygonStockTickers.contains(ticker)) {
			return ticker + COLLECTION_POSTFIX_STOCK_ONE_MINUTE;
		} else {
			return ticker + COLLECTION_POSTFIX_CRYPTO_ONE_MINUTE;
		}
	}

	private List<Bar> getLastBarsInTimeFrame(TimeFrame timeFrame, String collectionName, Long toDate) {
		int barsAmount = barLimit * timeFrame.getBasicElementsAmount();
		Query query = new Query();
		query.with(Sort.by(Sort.Order.desc("date")));
		query.addCriteria(Criteria.where("date").lte(toDate));
		return mongoTemplate.find(query.limit(barsAmount), Bar.class, collectionName);
	}
}

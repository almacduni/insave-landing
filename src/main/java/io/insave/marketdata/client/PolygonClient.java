package io.insave.marketdata.client;

import io.insave.marketdata.entity.AggregateBarsResponse;
import io.insave.marketdata.entity.TimeFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PolygonClient {

	@Value("${polygon.api_key}")
	private final String API_KEY;

	private final RestTemplate restTemplate;

	public AggregateBarsResponse getTickerAggregates(String ticker, TimeFrame timeFrame, long from, long to) {
		String url = "https://api.polygon.io/v2/aggs/ticker/{ticker}/range/{polygonTimeFrame}/{from}/{to}?unadjusted=false&sort=desc&limit=50000&apiKey={API_KEY}";
		return restTemplate.getForEntity(url, AggregateBarsResponse.class,
				ticker, timeFrame.getPolygonTimeFrame(), from, to, API_KEY).getBody();
	}
}

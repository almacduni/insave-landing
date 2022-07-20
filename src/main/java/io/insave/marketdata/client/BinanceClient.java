package io.insave.marketdata.client;

import io.insave.marketdata.entity.TimeFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BinanceClient {

	private final RestTemplate restTemplate;

	public List<List<Object>> getBarsPerMinute(String ticker, TimeFrame timeFrame, long startTime, long endTime) {
		return restTemplate.exchange(
				"https://api.binance.com/api/v3/klines?symbol={ticker}&interval={binanceTimeFrame}&startTime={startTime}&endTime={endTime}&limit=1000",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<List<Object>>>() {
				},
				ticker, timeFrame.getBinanceTimeFrame(), startTime, endTime)
				.getBody();
	}
}

package io.insave.marketdata.controller;

import io.insave.marketdata.entity.TimeFrame;
import io.insave.marketdata.service.marketdata.MarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/market-data")
public class MarketDataController {

	private final MarketDataService marketDataService;

	@PostMapping
	public ResponseEntity<?> storeMarketData(@RequestParam String ticker,
											 @RequestParam long daysAmount,
											 @RequestParam TimeFrame timeFrame) {
		marketDataService.storeMarketData(ticker, daysAmount, timeFrame);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}

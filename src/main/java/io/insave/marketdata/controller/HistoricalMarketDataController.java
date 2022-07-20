package io.insave.marketdata.controller;

import io.insave.marketdata.entity.Bar;
import io.insave.marketdata.entity.TimeFrame;
import io.insave.marketdata.service.marketdata.HistoricalMarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/market-data/historical")
public class HistoricalMarketDataController {

	private final HistoricalMarketDataService historicalMarketDataService;

	@GetMapping
	public ResponseEntity<List<Bar>> getMarketData(@RequestParam TimeFrame timeFrame,
												   @RequestParam String ticker,
												   @RequestParam(required = false) Long from,
												   @RequestParam Long to) {
		List<Bar> bars = historicalMarketDataService.getMarketData(timeFrame, ticker.toUpperCase(), from, to);
		return new ResponseEntity<>(bars, HttpStatus.OK);
	}
}

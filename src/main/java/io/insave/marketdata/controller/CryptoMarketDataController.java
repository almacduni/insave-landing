package io.insave.marketdata.controller;

import io.insave.marketdata.entity.TimeFrame;
import io.insave.marketdata.service.marketdata.CryptoMarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/market-data/crypto")
public class CryptoMarketDataController {

	private final CryptoMarketDataService cryptoMarketDataService;

	@PostMapping
	public ResponseEntity<?> storeCryptoMarketData(@RequestParam String ticker,
												   @RequestParam long daysAmount,
												   @RequestParam TimeFrame timeFrame) {
		cryptoMarketDataService.storeCryptoMarketData(ticker, daysAmount, timeFrame);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("/index")
	public ResponseEntity<?> storeIndexMarketData(@RequestParam TimeFrame timeFrame,
												  @RequestParam long daysAmount) {
		cryptoMarketDataService.storeIndexMarketData(daysAmount, timeFrame);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}

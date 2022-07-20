package io.insave.marketdata.controller;

import io.insave.marketdata.service.CryptoAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/crypto/assets")
public class CryptoAssetController {

	private final CryptoAssetService cryptoAssetService;

	@PostMapping
	public ResponseEntity<?> saveCryptoAssets() {
		cryptoAssetService.saveCryptoAssets();
		return new ResponseEntity<>(HttpStatus.OK);
	}
}

package io.insave.marketdata.service;

import io.insave.marketdata.client.CoinMarketCapClient;
import io.insave.marketdata.entity.CoinMarketCapCryptoAssetInfo;
import io.insave.marketdata.entity.CryptoAsset;
import io.insave.marketdata.repository.CryptoAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CryptoAssetService {

	private final CoinMarketCapClient coinMarketCapClient;
	private final CryptoAssetRepository cryptoAssetRepository;

	public void saveCryptoAssets() {
		cryptoAssetRepository.deleteAll();
		List<CoinMarketCapCryptoAssetInfo> assetInfoList = coinMarketCapClient.getCryptoTickers();
		List<CryptoAsset> cryptoTickers = assetInfoList.stream()
				.map(assetInfo -> {
					CryptoAsset cryptoAsset = new CryptoAsset();
					cryptoAsset.setId(UUID.randomUUID());
					cryptoAsset.setTicker(assetInfo.getName() + "USDT");
					cryptoAsset.setName(assetInfo.getCompanyName());
					cryptoAsset.setCirculatingSupply(assetInfo.getCirculatingSupply());
					return cryptoAsset;
				})
				.collect(Collectors.toList());
		cryptoAssetRepository.saveAll(cryptoTickers);
	}
}

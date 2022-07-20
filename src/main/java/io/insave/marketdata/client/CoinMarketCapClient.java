package io.insave.marketdata.client;

import io.insave.marketdata.entity.CoinMarketCapCryptoAssetInfo;
import io.insave.marketdata.entity.CoinMarketCapCryptoAssetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoinMarketCapClient {

	private static final String GET_CRYPTO_ASSET_INFO_URL = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";

	@Value("${coinmarketcap.api_key}")
	private final String API_KEY;
	private final RestTemplate restTemplate;

	public List<CoinMarketCapCryptoAssetInfo> getCryptoTickers() {
		String url = GET_CRYPTO_ASSET_INFO_URL + "?CMC_PRO_API_KEY=" + API_KEY;
		return restTemplate.getForEntity(url, CoinMarketCapCryptoAssetResponse.class).getBody().getData();
	}
}

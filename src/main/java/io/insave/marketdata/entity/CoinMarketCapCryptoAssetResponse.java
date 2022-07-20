package io.insave.marketdata.entity;

import lombok.Data;

import java.util.List;

@Data
public class CoinMarketCapCryptoAssetResponse {

	private List<CoinMarketCapCryptoAssetInfo> data;
}


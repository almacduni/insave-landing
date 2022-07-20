package io.insave.marketdata.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoinMarketCapCryptoAssetInfo {

	private Long id;

	@JsonAlias("symbol")
	private String name;

	@JsonAlias("slug")
	private String companyName;

	@JsonAlias("circulating_supply")
	private BigDecimal circulatingSupply;
}

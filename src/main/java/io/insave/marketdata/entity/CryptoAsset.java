package io.insave.marketdata.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class CryptoAsset {

	private UUID id;
	private String ticker;
	private String name;
	private BigDecimal circulatingSupply;
}

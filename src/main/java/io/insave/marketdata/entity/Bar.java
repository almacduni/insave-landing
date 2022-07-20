package io.insave.marketdata.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bar {

	@JsonAlias("o")
	private BigDecimal open;

	@JsonAlias("c")
	private BigDecimal close;

	@JsonAlias("h")
	private BigDecimal high;

	@JsonAlias("l")
	private BigDecimal low;

	@JsonAlias("E")
	private long date;

	@JsonAlias("s")
	private String ticker;

	public Bar(BigDecimal open, BigDecimal close, BigDecimal high, BigDecimal low, long date) {
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.date = date;
	}

}
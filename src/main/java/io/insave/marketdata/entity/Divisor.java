package io.insave.marketdata.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class Divisor {

	private BigDecimal divisor;
}
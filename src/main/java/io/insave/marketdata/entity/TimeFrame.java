package io.insave.marketdata.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TimeFrame {

	ONE_MINUTE("1m", "1/minute", null),
	FIVE_MINUTES("", "5/minute", 5),
	THIRTY_MINUTES("", "30/minute", 30),
	ONE_HOUR("", "60/minute", 60),
	FOUR_HOUR("", "240/minute", 240),
	ONE_DAY("1d", "1/day", null),
	ONE_WEEK("", "1/week", null),
	ONE_MONTH("", "1/month", null),
	ONE_YEAR("", "1/year", null);

	@Getter
	private final String binanceTimeFrame;

	@Getter
	private final String polygonTimeFrame;

	@Getter
	private final Integer basicElementsAmount;
}

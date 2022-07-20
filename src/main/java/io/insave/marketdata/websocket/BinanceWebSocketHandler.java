package io.insave.marketdata.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.insave.marketdata.entity.Bar;
import io.insave.marketdata.service.marketdata.CryptoMarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinanceWebSocketHandler extends TextWebSocketHandler {

	private final ObjectMapper objectMapper;
	private final CryptoMarketDataService cryptoMarketDataService;

	@Bean
	@SneakyThrows
	public WebSocketSession BinanceWebSocketClientSession() {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		container.setDefaultMaxTextMessageBufferSize(512 * 1024 * 512);
		WebSocketClient webSocketClient = new StandardWebSocketClient(container);

		// https://binance-docs.github.io/apidocs/futures/en/#all-market-mini-tickers-stream
		return webSocketClient.doHandshake(this, new WebSocketHttpHeaders(),
				URI.create("wss://fstream.binance.com/ws/!miniTicker@arr"))
				.get();
	}

	@Override
	@SneakyThrows
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		List<Bar> bars = objectMapper.readValue(message.getPayload(),
				new TypeReference<List<Bar>>() {
				});

		cryptoMarketDataService.createActualIndexBarFromWebsocketUpdate(bars);
	}
}
package io.insave.marketdata.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.insave.marketdata.service.marketdata.MarketDataService;
import io.insave.marketdata.websocket.entity.polygon.PolygonWebSocketRequest;
import io.insave.marketdata.websocket.entity.polygon.SecondAggregate;
import io.insave.marketdata.websocket.entity.polygon.mapper.AggregateToBarMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolygonWebSocketHandler extends TextWebSocketHandler {

	private static final String POLYGON_WEBSOCKET_URL = "wss://delayed.polygon.io/stocks";

	private final ObjectMapper objectMapper;
	private final MarketDataService marketDataService;
	private final WebSocketClient webSocketClient = new StandardWebSocketClient();
	private final WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();

	@Value("${polygon.api_key}")
	private final String API_KEY;

	private boolean isSubscribed;
	private boolean isAuthenticated;

	@Bean
	@SneakyThrows
	public WebSocketSession polygonWebSocketClientSession() {
		return createConnection();
	}

	@Override
	@SneakyThrows
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		if (!isAuthenticated) {
			PolygonWebSocketRequest request = new PolygonWebSocketRequest();
				request.setAction("auth");
			request.setParams(API_KEY);
			TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(request));
			session.sendMessage(textMessage);
			isAuthenticated = true;
			return;
		}

		if (!isSubscribed) {
			PolygonWebSocketRequest request = new PolygonWebSocketRequest();
			request.setAction("subscribe");
			request.setParams("A.AAPL,A.TSLA,A.MSFT,A.GOOGL,A.AMZN");
			TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(request));
			session.sendMessage(textMessage);
			isSubscribed = true;
		}
		List<SecondAggregate> bars;
		try {
			bars = objectMapper.readValue(message.getPayload(),
					new TypeReference<List<SecondAggregate>>() {
					});
		} catch (JsonProcessingException e) {
			log.error("Not parsable Json message");
			return;
		}
		log.info("bars " + message.getPayload());
		marketDataService.createActualMinuteBarFromWebsocketUpdate(AggregateToBarMapper.convertToBars(bars));
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		log.debug("Websocket session for {} is closed with code {} and reason {}",
				session.getRemoteAddress(), status.getCode(), status.getReason());
		isAuthenticated = false;
		isSubscribed = false;
		createConnection();
	}

	private WebSocketSession createConnection() throws InterruptedException, java.util.concurrent.ExecutionException {
		WebSocketSession webSocketSession = webSocketClient.doHandshake(this, webSocketHttpHeaders,
				URI.create(POLYGON_WEBSOCKET_URL))
				.get();
		log.debug("Websocket session for {} is created with id {}", webSocketSession.getRemoteAddress(),
				webSocketSession.getId());
		return webSocketSession;
	}
}
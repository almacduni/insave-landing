package io.insave.marketdata.websocket.entity.polygon;

import lombok.Data;

@Data
public class PolygonWebSocketRequest {

  private String action;
  private String params;
}

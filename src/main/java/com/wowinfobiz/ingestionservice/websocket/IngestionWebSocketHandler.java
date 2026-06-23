package com.wowinfobiz.ingestionservice.websocket;

import com.wowinfobiz.ingestionservice.service.IngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

public class IngestionWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(IngestionWebSocketHandler.class);

    private final JsonParser jsonParser = JsonParserFactory.getJsonParser();
    private final IngestionService ingestionService;

    public IngestionWebSocketHandler(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        try {
            Map<String, Object> rawPayload = jsonParser.parseMap(payload);
            ingestionService.saveRawReading(rawPayload);
        } catch (Exception ex) {
            log.warn("Invalid WebSocket payload from session {}: {}", session.getId(), payload, ex);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket disconnected: {} ({})", session.getId(), status);
    }
}

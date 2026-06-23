package com.wowinfobiz.ingestionservice.websocket;

import com.wowinfobiz.ingestionservice.service.IngestionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketIngestionConfig implements WebSocketConfigurer {

    private final IngestionService ingestionService;

    @Value("${ingestion.websocket.endpoint:/api/v1/ingestion/socket/ws}")
    private String endpoint;

    @Value("${ingestion.websocket.allowed-origins:*}")
    private String[] allowedOrigins;

    public WebSocketIngestionConfig(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new IngestionWebSocketHandler(ingestionService), endpoint)
                .setAllowedOrigins(allowedOrigins);
    }
}

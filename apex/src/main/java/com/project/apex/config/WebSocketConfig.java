package com.project.apex.config;

import com.project.apex.websocket.ClientWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import jakarta.annotation.PostConstruct;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ClientWebSocket clientWebSocket;

    @Autowired
    public WebSocketConfig(ClientWebSocket clientWebSocket) {
        this.clientWebSocket = clientWebSocket;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(clientWebSocket, "/ws").setAllowedOrigins("*"); // Adjust allowed origins in production
    }

    @PostConstruct
    public void initWebSocketConnection() {
        // Here, you can trigger any initialization logic if needed
        System.out.println("WebSocket server initialized and ready to accept connections.");
    }
}
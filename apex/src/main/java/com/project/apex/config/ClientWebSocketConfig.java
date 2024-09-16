package com.project.apex.config;

import com.project.apex.component.ClientWebSocket;
import com.project.apex.component.JwtHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocket
public class ClientWebSocketConfig implements WebSocketConfigurer {

    private final ClientWebSocket clientWebSocket;


    public ClientWebSocketConfig(ClientWebSocket clientWebSocket, JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.clientWebSocket = clientWebSocket;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(clientWebSocket, "/ws/**")
                .setAllowedOrigins("*");
    }
}

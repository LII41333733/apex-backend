package com.project.apex.config;

import com.project.apex.component.ClientWebSocket;
import com.project.apex.controller.TradeController;
import com.project.apex.service.AccountService;
import com.project.apex.service.OrdersService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import jakarta.annotation.PostConstruct;

import java.io.IOException;

@Configuration
@EnableWebSocket
public class ClientWebSocketConfig implements WebSocketConfigurer {

    private static final Logger logger = LogManager.getLogger(ClientWebSocketConfig.class);
    private final ClientWebSocket clientWebSocket;
    private final AccountService accountService;
    private final OrdersService ordersService;

    @Autowired
    public ClientWebSocketConfig(ClientWebSocket clientWebSocket, AccountService accountService, OrdersService ordersService) {
        this.clientWebSocket = clientWebSocket;
        this.accountService = accountService;
        this.ordersService = ordersService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(clientWebSocket, "/ws").setAllowedOrigins("*"); // Adjust allowed origins in production
    }

    @PostConstruct
    public void initWebSocketConnection() {
        // Here, you can trigger any initialization logic if needed
        logger.info("WebSocket server initialized and ready to accept connections.");
//        ordersService.startFetching();
//        accountService.init();
    }
}
package com.project.apex.component;

import com.project.apex.service.AccountService;
import com.project.apex.service.MarketService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class AccountStream extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(AccountStream.class);
    private final AtomicReference<WebSocketSession> sessionReference = new AtomicReference<>();
    private final ClientWebSocket clientWebSocket;

    @Autowired
    public AccountStream(ClientWebSocket clientWebSocket) {
        this.clientWebSocket = clientWebSocket;
    }

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        sessionReference.set(session);
        logger.info("Account stream connection established: Session ID: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming WebSocket messages
        String payload = message.getPayload();
        // Process the message here
        System.out.println(payload);
        clientWebSocket.sendMessageToAll(payload);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // Handle transport errors
        System.err.println("Error occurred in WebSocket connection: " + exception.getMessage());
        logger.error("Error occurred in WebSocket connection: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Handle WebSocket connection close events
        System.out.println("WebSocket connection closed with status: " + status);
        sessionReference.set(null); // Clear the session reference
    }

    public void startStream() {
        WebSocketConnectionManager manager = new WebSocketConnectionManager(
                new StandardWebSocketClient(),
                this,
                "wss://ws.tradier.com/v1/accounts/events"
        );
        manager.start();
    }

    // Method to send a message outside the WebSocketHandler
    public void sendMessageOutside(String message) throws IOException {
        WebSocketSession session = sessionReference.get();
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        } else {
            System.out.println("WebSocket session is not open.");
        }
    }
}
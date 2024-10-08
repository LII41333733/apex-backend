package com.project.apex.component;

import com.project.apex.service.MarketService;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.PostConstruct;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

@Component
public class MarketStream {
    private static final Logger logger = LoggerFactory.getLogger(MarketStream.class);


    private WebSocketClient webSocketClient;

    @Value("${websocket.uri}")
    private String websocketUri;

    private final MarketService marketService;


    public MarketStream(MarketService marketService) {
        this.marketService = marketService;
    }

    @PostConstruct
    public void connectAndStartStream() {
        try {
            createNewWebSocketClient();
            new Thread(() -> {
                try {
                    webSocketClient.connectBlocking(); // Blocking connect moved to a separate thread
                } catch (InterruptedException e) {
                    logger.error("Failed to connect WebSocket", e);
                    Thread.currentThread().interrupt(); // Restore interrupted state
                }
            }).start();
        } catch (URISyntaxException e) {
            logger.error("WebSocket URI is invalid", e);
        } catch (Exception e) {
            logger.error("Error during WebSocket connection", e);
        }
    }

    public void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
        } else {
            logger.info("WebSocket is not open. Cannot send message.");
        }
    }

    public void reconnect() throws URISyntaxException {
        createNewWebSocketClient();
        new Thread(() -> {
            try {
                webSocketClient.connectBlocking(); // Blocking connect in a new thread
                String message = marketService.buildOptionsStreamCall();
                this.sendMessage(message);
            } catch (InterruptedException e) {
                logger.error("Failed to reconnect WebSocket", e);
                Thread.currentThread().interrupt(); // Restore interrupted state
            }
        }).start();
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    // New method to stop all streams
    public void stopAllStreams() {
        if (webSocketClient != null) {
            webSocketClient.close();  // Close the WebSocket connection
            logger.info("WebSocket connection closed.");
        }
    }

    @PreDestroy
    public void onDestroy() {
        stopAllStreams();  // Ensure all streams are stopped when the bean is destroyed
    }

    private void createNewWebSocketClient() throws URISyntaxException {
        URI uri = new URI(websocketUri);
        webSocketClient = new WebSocketClient(uri) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                logger.info("Opened connection");
            }

            @Override
            public void onMessage(String message) {
//                logger.info("Market Stream message: " + message);

                try {
                    JsonNode jsonNode = new ObjectMapper().readTree(message);
                    String type = jsonNode.get("type").asText();

                    if (type.equals("quote")) {
                        marketService.sendOptionsQuote(message);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                logger.info("Connection closed by " + (remote ? "remote peer" : "us") + ". Code: " + code + ", Reason: " + reason);
            }

            @Override
            public void onError(Exception e) {
                logger.error("Exception in WebSocket connection", e);
            }
        };
    }
}

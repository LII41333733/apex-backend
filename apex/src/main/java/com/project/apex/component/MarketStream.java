package com.project.apex.component;

import com.project.apex.service.MarketService;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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

    public void startStream() {
        stopAllStreams();  // Ensure previous WebSocket is closed

        try {
            createNewWebSocketClient();  // Create new WebSocket instance
            webSocketClient.connectBlocking();  // Blocking connection setup

            String message = marketService.buildOptionsStreamCall();  // Get fresh message
            webSocketClient.send(message);  // Send the new message instance
        } catch (URISyntaxException | InterruptedException e) {
            logger.error("Failed to start a new WebSocket stream", e);
            Thread.currentThread().interrupt();
        }
    }

    public void stopAllStreams() {
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;  // Nullify the instance
            logger.info("Previous WebSocket connection closed.");
        }
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
                logger.debug("Market Stream message: " + message);
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

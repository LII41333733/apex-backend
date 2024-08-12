package com.project.apex.websocket;

import com.project.apex.config.InitConfig;
import com.project.apex.service.MarketService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

// Version 1.8.0_31
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MarketStream extends WebSocketClient {

    private static final Logger logger = LogManager.getLogger(MarketStream.class);

    private final InitConfig initConfig;
    private final MarketService marketService;

    @Autowired
    public MarketStream(InitConfig initConfig, MarketService marketService) throws URISyntaxException {
        super(new URI("wss://ws.tradier.com/v1/markets/events"));
        this.initConfig = initConfig;
        this.marketService = marketService;
    }

    public void connectAndStartStream() throws IOException, URISyntaxException {
        connect(); // Open the WebSocket connection
        new Thread(this::startOptionChainStream).start();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Opened connection");
        send(marketService.buildOptionsStreamCall());
    }

    @Override
    public void onMessage(String message) {
        logger.info("Received message: " + message);

        try {
            JsonNode jsonNode = new ObjectMapper().readTree(message);
            String type = jsonNode.get("type").asText();

            if (type.equals("quote")) {
                marketService.updateSymbolData(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
    }

    @Override
    public void onError(Exception e) {
        System.err.println("Exception: " + e.getMessage());
        // Attempt to reconnect on error
        reconnectWithDelay();    }

    public void startOptionChainStream() {
        if (this.isOpen()) {
        } else {
            logger.warn("WebSocket is not connected. Cannot start option chain stream.");
        }
    }

    private void reconnectWithDelay() {
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Wait for 5 seconds before attempting to reconnect
                this.reconnectBlocking();
                connectAndStartStream(); // Reconnect and restart the stream
            } catch (InterruptedException | IOException | URISyntaxException e) {
                logger.error("Failed to reconnect: " + e.getMessage(), e);
            }
        }).start();
    }

    public InitConfig getInitConfig() {
        return initConfig;
    }
}
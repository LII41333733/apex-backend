package com.project.apex.component;

import com.project.apex.config.EnvConfig;
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

    private final EnvConfig envConfig;
    private final MarketService marketService;

    @Autowired
    public MarketStream(EnvConfig envConfig, MarketService marketService) throws URISyntaxException {
        super(new URI("wss://ws.tradier.com/v1/markets/events"));
        this.envConfig = envConfig;
        this.marketService = marketService;
    }

    public void connectAndStartStream() throws IOException, URISyntaxException {
       this.connect(); // Open the WebSocket connection
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.info("Opened Options Chain Stream");
        send(marketService.buildOptionsStreamCall());
    }

    @Override
    public void onMessage(String message) {
        logger.info("Market Stream message: " + message);

        try {
            JsonNode jsonNode = new ObjectMapper().readTree(message);
            System.out.println(jsonNode.toString());
            String type = jsonNode.get("type").asText();

//            System.out.println(jsonNode.get("sumbol").asText());

            if (type.equals("quote")) {
                System.out.println(jsonNode.toString());
                String s = jsonNode.get("symbol").asText();
                System.out.println(s);
//            if (s.equals("SPY240815C00552000")) {
//                System.out.println(jsonNode.toString());
//            }

                marketService.sendOptionsQuote(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
//        reconnectWithDelay(); // Handle reconnection when the connection is closed
    }

    @Override
    public void onError(Exception e) {
        logger.error("Exception in WebSocket connection: " + e.getMessage());
//        reconnectWithDelay();
    }

    public MarketStream createAndConnectNewWebSocketClient() {
        try {
            MarketStream newClient = new MarketStream(envConfig, marketService);
            newClient.connectAndStartStream(); // Start a new connection
            return newClient; // Return the new instance if successful
        } catch (URISyntaxException | IOException e) {
            logger.error("Failed to create and connect a new WebSocket client for Market Stream", e);
            return null;
        }
    }


    public EnvConfig getEnvConfig() {
        return envConfig;
    }
}
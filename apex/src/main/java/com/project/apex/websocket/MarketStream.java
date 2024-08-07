package com.project.apex.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.apex.config.InitConfig;
import com.project.apex.model.LiveOption;
import com.project.apex.service.MarketService;
import com.project.apex.utils.ApiRequest;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

// Version 1.8.0_31
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
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
        if (this.isClosed()) {
            connect(); // Open the WebSocket connection
        }
        new Thread(this::startOptionChainStream).start();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Opened connection");
        synchronized (this) {
            notifyAll(); // Notify any waiting threads
        }
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
        } catch (JsonProcessingException e) {
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
    }

    public void startOptionChainStream() {
//        synchronized (this) {
//            // Wait until the WebSocket connection is open
//            try {
//                wait(); // Wait until notified
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        send(marketService.buildOptionsStreamCall());
    }
}
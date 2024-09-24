package com.project.apex.component;

import com.project.apex.service.AccountService;
import com.project.apex.service.MarketService;
import com.project.apex.service.OrdersService;
import com.project.apex.service.TradeService;
import com.project.apex.util.Convert;
import com.project.apex.util.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ClientWebSocket extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientWebSocket.class);
    private final AccountService accountService;
    private final TradeService tradeService;
    private final OrdersService ordersService;
    private final MarketStream marketStream;
    private final MarketService marketService;

    // A thread-safe list to store all active WebSocket sessions
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Autowired
    public ClientWebSocket(
            AccountService accountService,
           TradeService tradeService,
           MarketStream marketStream,
           @Lazy OrdersService ordersService,
           MarketService marketService) {
        this.accountService = accountService;
        this.tradeService = tradeService;
        this.ordersService = ordersService;
        this.marketStream = marketStream;
        this.marketService = marketService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        logger.info("WebSocket connection established: " + session.getId());
        sessions.add(session);
        sendData(new Record<>("balance", accountService.getBalanceData()));
        sendData(new Record<>("trades", tradeService.fetchTrades()));
        marketService.fetchMarketPrices();
        ordersService.fetchOrders();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Transport error: " + session.getId(), exception);
        // Check if the session is still open before closing
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            // Mark as disconnected only if there are no active sessions left
            setConnected(false);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        logger.info("WebSocket connection closed: " + session.getId() + " with status " + status);
        if (sessions.isEmpty()) {
            setConnected(false);
            marketStream.stopAllStreams();
        }
    }

    public void sendData(Object object) throws IOException {
        sendMessageToAll(Convert.objectToString(object));
    }

    public void sendMessageToAll(String message) throws IOException {
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                } else {
                    logger.warn("Session is not open: " + session.getId());
                }
            } catch (IOException e) {
                logger.error("IOException: " + e.getMessage(), e);
                // Close session and remove from list if there's an issue
                session.close(CloseStatus.SERVER_ERROR);
                sessions.remove(session);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

//    @Scheduled(fixedRate = 3000)
    @Scheduled(fixedRate = 5000)
    public void fetchOrdersScheduleActive() {
        if (!sessions.isEmpty()) {
            try {
                ordersService.fetchOrders();
                marketService.fetchMarketPrices();
                sendData(new Record<>("balance", accountService.getBalanceData()));
                sendData(new Record<>("trades", tradeService.fetchTrades()));
            } catch (Exception e) {
                logger.error("Failed to fetch orders", e);
            }
        }
    }

    public boolean isConnected() {
        return !sessions.isEmpty();
    }

    public void setConnected(boolean connected) {
        if (!connected) {
            sessions.clear();
        }
    }
}

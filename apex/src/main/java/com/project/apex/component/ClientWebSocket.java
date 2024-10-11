package com.project.apex.component;

import com.project.apex.data.trades.TradeFactory;
import com.project.apex.service.AccountService;
import com.project.apex.service.MarketService;
import com.project.apex.service.OrdersService;
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
    private final OrdersService ordersService;
    private final MarketStream marketStream;
    private final MarketService marketService;
    private final TradeFactory tradeFactory;

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Autowired
    public ClientWebSocket(
            AccountService accountService,
            MarketStream marketStream,
            @Lazy OrdersService ordersService,
            MarketService marketService,
            TradeFactory tradeFactory) {
        this.accountService = accountService;
        this.ordersService = ordersService;
        this.marketStream = marketStream;
        this.marketService = marketService;
        this.tradeFactory = tradeFactory;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        logger.info("WebSocket connection established: " + session.getId());
        sessions.add(session);
        sendData(new Record<>("balance", accountService.getBalanceData()));
        sendData(new Record<>("trades", tradeFactory.fetchAllTrades()));
        marketService.fetchMarketPrices();
        ordersService.fetchOrders();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Transport error: " + session.getId(), exception);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
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
                session.close(CloseStatus.SERVER_ERROR);
                sessions.remove(session);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Scheduled(fixedRate = 30000)
    public void fetchOrdersScheduleActive() {
        if (!sessions.isEmpty()) {
            try {
                ordersService.fetchOrders();
                marketService.fetchMarketPrices();
                sendData(new Record<>("balance", accountService.getBalanceData()));
                sendData(new Record<>("trades", tradeFactory.fetchAllTrades()));
            } catch (Exception e) {
                logger.error("Failed to fetch orders", e);
            }
        }
    }

    @Scheduled(fixedRate = 5000)
    public void fetchOrdersSchedule() {
        if (sessions.isEmpty()) {
            ordersService.fetchOrders();
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

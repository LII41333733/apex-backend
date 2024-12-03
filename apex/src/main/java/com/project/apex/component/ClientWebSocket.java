package com.project.apex.component;

import com.project.apex.config.EnvConfig;
import com.project.apex.data.account.Balance;
import com.project.apex.data.trades.TradeFactory;
import com.project.apex.data.websocket.WebSocketData;
import com.project.apex.model.Trade;
import com.project.apex.service.AccountService;
import com.project.apex.service.MarketService;
import com.project.apex.service.OrdersService;
import com.project.apex.util.Convert;
import com.project.apex.util.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    private final Portfolio portfolio;
    private final DemoPortfolio demoPortfolio;
    private final EnvConfig envConfig;

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Autowired
    public ClientWebSocket(@Lazy OrdersService ordersService,
                           @Lazy Portfolio portfolio,
                           @Lazy DemoPortfolio demoPortfolio,
                           AccountService accountService,
                           MarketStream marketStream,
                           MarketService marketService,
                           EnvConfig envConfig) {
        this.ordersService = ordersService;
        this.accountService = accountService;
        this.marketStream = marketStream;
        this.marketService = marketService;
        this.portfolio = portfolio;
        this.demoPortfolio = demoPortfolio;
        this.envConfig = envConfig;
    }

    public void handleActiveClientWebSocketData() throws Exception {
        marketService.fetchMarketPrices();
        if (envConfig.isDemo()) {
            List<Trade> allTrades = demoPortfolio.fetchAllTrades();
            sendData(new Record<>(WebSocketData.DEMO_TRADES.name(), allTrades));
            Balance balance = new Balance();

            sendData(new Record<>(WebSocketData.BALANCE.name(), balance));
        } else {
            sendData(new Record<>(WebSocketData.BALANCE.name(), accountService.getBalanceData()));
            ordersService.fetchOrders();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: " + session.getId());
        sessions.add(session);
        handleActiveClientWebSocketData();
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
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        logger.info("WebSocket connection closed: " + session.getId() + " with status " + status);
        if (sessions.isEmpty()) {
            setConnected(false);
            marketStream.stopAllStreams();
        }
    }

    public void sendData(Object object) throws IOException {
        if (isConnected()) {
            for (WebSocketSession session : sessions) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(Convert.objectToString(object)));
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
    }

    public boolean isConnected() {
        return !sessions.isEmpty();
    }

    public void setConnected(boolean connected) {
        if (!connected) {
            sessions.clear();
        }
    }

    public List<WebSocketSession> getSessions() {
        return sessions;
    }
}

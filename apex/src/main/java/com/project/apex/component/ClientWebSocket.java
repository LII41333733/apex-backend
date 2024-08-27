package com.project.apex.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.data.Balance;
import com.project.apex.service.AccountService;
import com.project.apex.service.OrdersService;
import com.project.apex.service.TradeService;
import com.project.apex.util.Convert;
import com.project.apex.util.Record;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
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
    private final AccountState accountState;
    private final OrdersService ordersService;

    // A thread-safe list to store all active WebSocket sessions
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Autowired
    public ClientWebSocket(@Lazy AccountService accountService,
                           @Lazy TradeService tradeService,
                           AccountState accountState,
                           @Lazy OrdersService ordersService) {
        this.accountService = accountService;
        this.tradeService = tradeService;
        this.accountState = accountState;
        this.ordersService = ordersService;
    }

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws IOException {
        logger.info("WebSocket connection established: " + session.getId());
        sessions.add(session);
        sendData(new Record<>("balance", accountService.getBalanceData()));
        ordersService.fetchOrders();
//        ordersService.startOrderFetching();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if (exception instanceof IOException) {
            logger.error("Transport error (IOException): " + session.getId(), exception);
        } else {
            logger.error("Transport error: " + session.getId(), exception);
        }
        session.close(CloseStatus.SERVER_ERROR);
;    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove the session from the list of active sessions
        sessions.remove(session);
        logger.info("WebSocket connection closed: " + session.getId() + " with status " + status);

//        ordersService.stopOrderFetching();
    }

    public void sendData(Object object) throws IOException {
        sendMessageToAll(Convert.objectToString(object));
    }

    public void sendMessageToAll(String message) throws IOException {
        logger.info("Sending message to all connected clients: " + message);

        // Iterate over all active sessions and send the message
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
//                logger.info("Message sent to session: " + session.getId());
            } else {
                logger.warn("Session is not open: " + session.getId());
            }
        }
    }
}
package com.project.apex.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    // A thread-safe list to store all active WebSocket sessions
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Add the session to the list of active sessions
        sessions.add(session);
        logger.info("WebSocket connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        HttpHeaders headers = session.getHandshakeHeaders();
        System.out.println("Handshake received: " + headers.toString());

        System.out.println("Received: " + payload);

        // Echo the received message back to the client
        session.sendMessage(new TextMessage("Echo: " + payload));
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
    }

    public void sendMessageToAll(String message) throws IOException {
        logger.info("Sending message to all connected clients: " + message);

        // Iterate over all active sessions and send the message
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
                logger.info("Message sent to session: " + session.getId());
            } else {
                logger.warn("Session is not open: " + session.getId());
            }
        }
    }
}
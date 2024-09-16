package com.project.apex.component;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        java.util.List<String> protocols = request.getHeaders().get("Sec-WebSocket-Protocol");
        if (protocols != null && protocols.contains("authorization")) {
            String token = protocols.get(protocols.indexOf("authorization") + 1);
            // Validate the token here and set user in attributes if needed
            attributes.put("token", token);
            response.getHeaders().add("Sec-WebSocket-Protocol", "authorization");
            return true; // Accept the handshake
        }

        return false; // Reject the handshake if protocol is not correct or token is invalid
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Do nothing here
    }
}

// Locație: gateway/src/main/java/com/loyalty/gateway/websocket/NotificationHandler.java
package com.loyalty.gateway.websocket;

import com.loyalty.gateway.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NotificationHandler {

    private final JwtTokenProvider tokenProvider;

    @MessageMapping("/authenticate")
    public void authenticate(@Payload Map<String, String> payload,
                             SimpMessageHeaderAccessor headerAccessor) {
        String token = payload.get("token");

        if (token != null && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromToken(token);
            headerAccessor.getSessionAttributes().put("username", username);
            log.info("WebSocket authenticated for user: {}", username);
        } else {
            log.warn("WebSocket authentication failed - invalid token");
        }
    }

    @MessageMapping("/ping")
    public void handlePing(Principal principal) {
        if (principal != null) {
            log.debug("Ping received from: {}", principal.getName());
        }
    }

    @MessageMapping("/subscribe")
    public void handleSubscription(@Payload Map<String, String> payload, Principal principal) {
        String channel = payload.get("channel");
        if (principal != null) {
            log.info("User {} subscribed to channel: {}", principal.getName(), channel);
        }
    }
}
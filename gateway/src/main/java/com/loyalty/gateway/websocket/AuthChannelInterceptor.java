package com.loyalty.gateway.websocket;

import com.loyalty.gateway.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    @SuppressWarnings("null")
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            log.info("WebSocket command: {} | sessionId: {}", accessor.getCommand(), accessor.getSessionId());

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String authToken = accessor.getFirstNativeHeader("Authorization");
                Authentication authentication = null;

                // 1. Try Token from Header
                if (authToken != null && authToken.startsWith("Bearer ")) {
                    String token = authToken.substring(7);
                    authentication = getAuthentication(token);
                    if (authentication != null) {
                        log.info("WebSocket authenticated from Header: {}", authentication.getName());
                    }
                }

                // 2. Try Token from Session Attributes (populated by HandshakeInterceptor)
                if (authentication == null) {
                    Map<String, Object> attributes = accessor.getSessionAttributes();
                    if (attributes != null && attributes.containsKey("user")) {
                        authentication = (Authentication) attributes.get("user");
                        log.info("WebSocket authenticated from Handshake Attributes: {}", authentication.getName());
                    }
                }

                if (authentication != null) {
                    accessor.setUser(authentication);
                    // Critical: Propagate the updated accessor headers to the message
                    return org.springframework.messaging.support.MessageBuilder.createMessage(message.getPayload(),
                            accessor.getMessageHeaders());
                } else {
                    log.warn("WebSocket CONNECT: Authentication failed (no valid token in header or handshake)");
                }
            }
        }
        return message;
    }

    private Authentication getAuthentication(String token) {
        try {
            if (tokenProvider.validateToken(token)) {
                String username = tokenProvider.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                return new UsernamePasswordAuthenticationToken(
                        username, null, userDetails.getAuthorities());
            }
        } catch (Exception e) {
            log.error("Error authenticating WebSocket token", e);
        }
        return null;
    }
}

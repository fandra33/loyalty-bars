package com.loyalty.gateway.websocket;

import com.loyalty.gateway.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    @SuppressWarnings("null")
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.info("WebSocket Handshake started: {} | From: {}", request.getURI(), request.getRemoteAddress());

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token != null) {
                try {
                    if (tokenProvider.validateToken(token)) {
                        String username = tokenProvider.getUsernameFromToken(token);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                username, null, userDetails.getAuthorities());

                        attributes.put("user", authentication);
                        log.info("WebSocket Handshake authenticated user: {}", username);
                    } else {
                        log.warn("WebSocket Handshake: Invalid token provided via query parameter");
                    }
                } catch (Exception e) {
                    log.error("WebSocket Handshake: Error validating token", e);
                }
            } else {
                log.info("WebSocket Handshake: No token found in query parameters");
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("null")
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, @Nullable Exception exception) {
        if (exception != null) {
            log.error("WebSocket Handshake failed for {}: {}", request.getURI(), exception.getMessage());
        } else {
            log.info("WebSocket Handshake completed successfully for {}", request.getURI());
        }
    }
}

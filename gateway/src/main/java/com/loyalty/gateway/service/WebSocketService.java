// Locație: gateway/src/main/java/com/loyalty/gateway/service/WebSocketService.java
package com.loyalty.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loyalty.gateway.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyPointsUpdate(User user, String eventType, Integer points) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", eventType);
        notification.put("points", points);
        notification.put("newBalance", user.getPointsBalance());
        notification.put("timestamp", LocalDateTime.now().toString());
        notification.put("message", buildMessage(eventType, points));

        sendToUser(user.getEmail(), "/queue/notifications", notification);
    }

    public void notifyTransaction(String userEmail, String transactionType, Integer points, String barName) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "TRANSACTION_" + transactionType);
        notification.put("points", points);
        notification.put("barName", barName);
        notification.put("timestamp", LocalDateTime.now().toString());

        sendToUser(userEmail, "/queue/transactions", notification);
    }

    public void notifyNewReward(String barName, String rewardName, Integer pointsCost) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NEW_REWARD");
        notification.put("barName", barName);
        notification.put("rewardName", rewardName);
        notification.put("pointsCost", pointsCost);
        notification.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/rewards", notification);
    }

    private void sendToUser(String userEmail, String destination, Object payload) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userEmail,
                    destination,
                    payload);
            log.info("WebSocket notification sent to user: {} on {}", userEmail, destination);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user: {}", userEmail, e);
        }
    }

    public void broadcast(String destination, Object payload) {
        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.debug("WebSocket broadcast sent to: {}", destination);
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket message to: {}", destination, e);
        }
    }

    private String buildMessage(String eventType, Integer points) {
        return switch (eventType) {
            case "POINTS_EARNED" -> String.format("You earned %d points!", points);
            case "REWARD_REDEEMED" -> String.format("You redeemed a reward for %d points!", points);
            case "TRANSACTION_VALIDATED" -> String.format("Transaction validated! +%d points", points);
            default -> "Account updated";
        };
    }

    public void notifyEvent(String userEmail, String eventType, Map<String, Object> data) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", eventType);
        notification.put("timestamp", LocalDateTime.now().toString());
        notification.putAll(data);

        sendToUser(userEmail, "/queue/events", notification);
    }
}

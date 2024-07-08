package com.example.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class MessageService {

    private final MessageQueueService messageQueueService;

    private final LeastLoadedQueueService leastLoadedQueueService;

    private final RedisCacheService redisCacheService;

    @Autowired
    public MessageService(MessageQueueService messageQueueService, LeastLoadedQueueService leastLoadedQueueService, RedisCacheService redisCacheService) {
        this.messageQueueService = messageQueueService;
        this.leastLoadedQueueService = leastLoadedQueueService;
        this.redisCacheService = redisCacheService;
    }

    public void sendInsertCommandMessage(String requestId, long timestamp, String producerId, String sessionId) throws Exception {
        Map<String, Object> message = createBaseMessage("InsertCommand");
        Map<String, Object> data = createInsertCommandData(requestId, timestamp, producerId, sessionId);
        message.put("data", data);
        sendMessage(message);
    }

    public void sendFindCommandMessage(String requestId, String sessionId) {
        Map<String, Object> message = createBaseMessage("FindCommand");
        Map<String, Object> data = createFindCommandData(requestId, sessionId);
        message.put("data", data);
        sendToLeastLoadedQueue(message);
    }

    public void sendSessionIdsMessage(String userId, List<String> sessionIds) {
        Map<String, Object> message = createBaseMessage("SessionIdsMessage");
        Map<String, Object> data = createSessionIdsData(userId, sessionIds);
        message.put("data", data);
        sendToLeastLoadedQueue(message);
    }

    private Map<String, Object> createBaseMessage(String type) {
        Map<String, Object> message = new HashMap<>();
        message.put("version", "1.0");
        message.put("type", type);
        message.put("timestamp", Instant.now().toString());
        message.put("metadata", createMetadata());
        return message;
    }

    private Map<String, Object> createInsertCommandData(String requestId, long timestamp, String producerId, String sessionId) {
        Map<String, Object> data = new HashMap<>();
        data.put("requestId", requestId);
        data.put("timestamp", timestamp);
        data.put("producerId", producerId);
        data.put("sessionId", sessionId);
        return data;
    }

    private Map<String, Object> createFindCommandData(String requestId, String sessionId) {
        Map<String, Object> data = new HashMap<>();
        data.put("requestId", requestId);
        data.put("sessionId", sessionId);
        return data;
    }

    private Map<String, Object> createSessionIdsData(String userId, List<String> sessionIds) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("sessionIds", sessionIds);
        return data;
    }

    private Map<String, String> createMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("source", "GATEWAY_INSTANCE_1");
        metadata.put("correlationId", UUID.randomUUID().toString());
        return metadata;
    }

    void sendMessage(Map<String, Object> message) throws Exception {
        var messageString = new ObjectMapper().writeValueAsString(message);
        try {
            messageQueueService.sendMessage(messageString);
            log.info("Sent message: {}", message);
        } catch (Exception e) {
            log.error("Failed to send message: {}", message, e);
            throw e;
        }
    }

    void sendToLeastLoadedQueue(Map<String, Object> message) {
        var leastLoadedQueue = leastLoadedQueueService.determineLeastLoadedQueueAndIncrementLoad();
        message.put("queueName", leastLoadedQueue);

        try {
            sendMessage(message);
        } catch (Exception e) {
            log.error("Failed to send message.{}", e.getMessage());
            decrementQueueLoad(leastLoadedQueue);
        }
    }

    public void decrementQueueLoad(String queueName) {
        var currentLoad = redisCacheService.getQueueLoad(queueName);
        if (currentLoad != null && currentLoad > 0) {
            redisCacheService.saveQueueLoad(queueName, currentLoad - 1);
            log.info("Decremented queue load for {}: {}", queueName, currentLoad - 1);
        }
    }

}

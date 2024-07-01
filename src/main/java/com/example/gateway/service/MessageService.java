package com.example.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageService {

    @Autowired
    private MessageQueueService messageQueueService;

    public void sendInsertCommandMessage(String requestId, long timestamp, String producerId, String sessionId) throws Exception {
        Map<String, Object> message = createBaseMessage("InsertCommand");
        Map<String, Object> data = createInsertCommandData(requestId, timestamp, producerId, sessionId);
        message.put("data", data);
        sendMessage(message);
    }

    public void sendFindCommandMessage(String requestId, String sessionId) throws Exception {
        Map<String, Object> message = createBaseMessage("FindCommand");
        Map<String, Object> data = createFindCommandData(requestId, sessionId);
        message.put("data", data);
        sendMessage(message);
    }

    public void sendSessionIdsMessage(String userId, List<String> sessionIds) throws Exception {
        Map<String, Object> message = createBaseMessage("SessionIdsMessage");
        Map<String, Object> data = createSessionIdsData(userId, sessionIds);
        message.put("data", data);
        sendMessage(message);
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

    private void sendMessage(Map<String, Object> message) throws Exception {
        String messageString = new ObjectMapper().writeValueAsString(message);
        messageQueueService.sendMessage(messageString);
    }
}

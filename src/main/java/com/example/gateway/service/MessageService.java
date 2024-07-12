package com.example.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class MessageService {

    private final MessageQueueService messageQueueService;
    private final LeastLoadedQueueService leastLoadedQueueService;
    private final RedisCacheService redisCacheService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MessageService(MessageQueueService messageQueueService, LeastLoadedQueueService leastLoadedQueueService, RedisCacheService redisCacheService, ObjectMapper objectMapper) {
        this.messageQueueService = messageQueueService;
        this.leastLoadedQueueService = leastLoadedQueueService;
        this.redisCacheService = redisCacheService;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(Map<String, Object> message) throws Exception {
        var messageString = objectMapper.writeValueAsString(message);
        try {
            messageQueueService.sendMessage(messageString);
            log.info("Sent message: {}", message);
        } catch (Exception e) {
            log.error("Failed to send message: {}", message, e);
            throw e;
        }
    }

    public void sendToLeastLoadedQueue(Map<String, Object> message) {
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

    public String fetchMessage(String queueKey) {
        return messageQueueService.fetchMessage(queueKey);
    }
}

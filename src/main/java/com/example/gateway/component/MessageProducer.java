package com.example.gateway.component;

import com.example.gateway.service.MessageQueueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * MessageProducer is responsible for generating
 * and sending messages to the message queue at fixed intervals.
 */
@Slf4j
@Component
public class MessageProducer {

    private static final String DEFAULT_USER_ID = "1234";

    private final MessageQueueService messageQueueService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MessageProducer(MessageQueueService messageQueueService, ObjectMapper objectMapper) {
        this.messageQueueService = messageQueueService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRate = 5000)
    public void produceMessages() {
        String userId = determineUserId();
        String message = createMessage(userId);
        messageQueueService.sendMessage(message);
        log.info("Produced message for userId {}: {}", userId, message);
    }

    private String determineUserId() {
        // Implement logic to set userId explicitly if needed, otherwise use default
        return DEFAULT_USER_ID;
    }

    String createMessage(String userId) {
        try {
            MessagePayload payload = new MessagePayload(
                    UUID.randomUUID().toString(),
                    Instant.now().toString(),
                    userId,
                    "GET",
                    "/statistics/" + userId + "/sessions"
            );

            return objectMapper.writeValueAsString(payload);

        } catch (Exception e) {
            log.error("Failed to create message", e);
            return "{}";
        }
    }



    static class MessagePayload {
        public String id;
        public String timestamp;
        public String userId;
        public Request request;

        public MessagePayload(String id, String timestamp, String userId, String method, String url) {
            this.id = id;
            this.timestamp = timestamp;
            this.userId = userId;
            this.request = new Request(method, url);
        }
    }

    static class Request {
        public String method;
        public String url;

        public Request(String method, String url) {
            this.method = method;
            this.url = url;
        }
    }
}

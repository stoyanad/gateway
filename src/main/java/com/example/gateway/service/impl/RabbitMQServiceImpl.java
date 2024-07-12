package com.example.gateway.service.impl;

import com.example.gateway.service.LeastLoadedQueueService;
import com.example.gateway.service.MessageQueueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMQServiceImpl implements MessageQueueService {

    private final RabbitTemplate rabbitTemplate;
    private final LeastLoadedQueueService leastLoadedQueueService;
    private final ObjectMapper objectMapper;

    @Autowired
    public RabbitMQServiceImpl(RabbitTemplate rabbitTemplate, LeastLoadedQueueService leastLoadedQueueService, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.leastLoadedQueueService = leastLoadedQueueService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendMessage(String message) {
        String queueName = leastLoadedQueueService.determineLeastLoadedQueueAndIncrementLoad();

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);

            MessageProperties properties = new MessageProperties();
            properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            properties.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Message rabbitMessage = new Message(jsonMessage.getBytes(), properties);

            rabbitTemplate.convertAndSend(queueName, rabbitMessage);
            log.info("Message sent to queue '{}': {}", queueName, jsonMessage);
        } catch (Exception e) {
            throw new ListenerExecutionFailedException("Failed to send message to RabbitMQ", e);
        }
    }

    @Override
    public String fetchMessage(String queueKey) {
        try {
            Object messageObject = rabbitTemplate.receiveAndConvert(queueKey);
            String message = messageObject != null ? messageObject.toString() : null;
            if (message != null) {
                log.info("Received message from queue '{}': {}", queueKey, message);
                return message;
            } else {
                return "No message available in queue: " + queueKey;
            }
        } catch (Exception e) {
            throw new ListenerExecutionFailedException("Failed to fetch message from RabbitMQ", e);
        }
    }
}

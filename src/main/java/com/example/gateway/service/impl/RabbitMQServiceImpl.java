package com.example.gateway.service.impl;

import com.example.gateway.service.MessageQueueService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQServiceImpl implements MessageQueueService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queueName}")
    private String queueName;

    @Override
    public void sendMessage(String message) {
        try {
            rabbitTemplate.convertAndSend(queueName, message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to RabbitMQ", e);
        }
    }

    @Override
    public String receiveMessage() {
        try {
            Object message = rabbitTemplate.receiveAndConvert(queueName);
            if (message != null) {
                return message.toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to receive message from RabbitMQ", e);
        }
    }
}

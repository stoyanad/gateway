package com.example.gateway.service.impl;

import com.example.gateway.service.LeastLoadedQueueService;
import com.example.gateway.service.MessageQueueService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQServiceImpl implements MessageQueueService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private LeastLoadedQueueService leastLoadedQueueService;


    @Override
    public void sendMessage(String message) {
        try {
            String queueName = leastLoadedQueueService.determineLeastLoadedQueueAndIncrementLoad();
            MessageProperties properties = new MessageProperties();
            properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            Message rabbitMessage = new Message(message.getBytes(), properties);
            rabbitTemplate.send(queueName, rabbitMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to RabbitMQ", e);
        }
    }
}

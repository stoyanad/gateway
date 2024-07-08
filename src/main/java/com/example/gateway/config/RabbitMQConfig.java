package com.example.gateway.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_INSTANCE = "queue_instance_";
    @Value("${rabbitmq.other_internal_service.instances}")
    private int instances;

    @Bean
    public List<Queue> queues() {
        List<Queue> queues = new ArrayList<>();
        for (int i = 1; i <= instances; i++) {
            queues.add(new Queue(QUEUE_INSTANCE + i));
        }
        return queues;
    }
}

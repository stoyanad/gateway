package com.example.gateway.component;

import com.example.gateway.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * QueueProcessor handles the processing of messages from multiple queues.
 * It periodically checks each queue for messages,
 * processes them using MessageConsumer, and manages the queue load.
 */
@Slf4j
@Component
public class QueueProcessor {

    private final MessageService messageService;
    private final MessageConsumer messageConsumer;

    @Value("${rabbitmq.other_internal_service.instances}")
    private int instances;

    public static final String QUEUE_INSTANCE = "queue_instance_";

    @Autowired
    public QueueProcessor(MessageService messageService, MessageConsumer messageConsumer) {
        this.messageService = messageService;
        this.messageConsumer = messageConsumer;
    }

    @Scheduled(fixedRate = 5000, initialDelay = 10000) // Add initial delay to allow queue declaration
    public void processQueue() {
        for (int i = 1; i <= instances; i++) {
            String queueKey = QUEUE_INSTANCE + i;
            String message = messageService.fetchMessage(queueKey);
            if (message != null) {
                messageConsumer.processQueueMessage(queueKey, message);
                messageService.decrementQueueLoad(queueKey);
            } else {
                log.warn("No message found in queue {}", queueKey);
            }
        }
    }
}

package com.example.gateway.component;

import com.example.gateway.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QueueProcessor {

    @Autowired
    private MessageService messageService;

    @Autowired
    private OtherInternalService otherInternalService;

    @Autowired
    private ResponseService responseService;

    @Value("${rabbitmq.other_internal_service.instances}")
    private int instances;

    public static final String QUEUE_INSTANCE = "queue_instance_";

    @Scheduled(fixedRate = 5000)
    public void processQueue() {
        for (int i = 1; i <=instances; i++) {
            String queueKey = QUEUE_INSTANCE + i;
            String message = fetchMessageFromQueue(queueKey);
            String response = otherInternalService.sendRequest();
            saveResponseToDatabase(queueKey, response);
            messageService.decrementQueueLoad(queueKey);
        }
    }

    private String fetchMessageFromQueue(String queueKey) {

        return "exampleMessage";
    }

    private void saveResponseToDatabase(String queueKey, String response) {
        responseService.saveResponse(queueKey, response);
    }
}

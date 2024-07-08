package com.example.gateway.service;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Service
@Setter
public class LeastLoadedQueueService {

    public static final String QUEUE_INSTANCE_PREFIX = "queue_instance_";

    @Value("${rabbitmq.other_internal_service.instances}")
    private int instances;

    @Autowired
    private RedisCacheService redisCacheService;

    public String determineLeastLoadedQueueAndIncrementLoad() {
        var queueLoads = getQueueLoads();

        String leastLoadedQueue = queueLoads.entrySet().stream()
                .min(Comparator.comparingInt(entry -> entry.getValue() == null ? 0 : entry.getValue()))
                .map(entry -> QUEUE_INSTANCE_PREFIX + entry.getKey())
                .orElse(QUEUE_INSTANCE_PREFIX + 1);

        Integer currentLoad = redisCacheService.getQueueLoad(leastLoadedQueue);
        redisCacheService.saveQueueLoad(leastLoadedQueue, (currentLoad == null ? 0 : currentLoad) + 1);

        return leastLoadedQueue;
    }

    public Map<Integer, Integer> getQueueLoads() {
        Map<Integer, Integer> queueLoads = new HashMap<>();
        for (int i = 1; i <= instances; i++) {
            String queueKey = QUEUE_INSTANCE_PREFIX + i;
            Integer load = redisCacheService.getQueueLoad(queueKey);
            queueLoads.put(i, load == null ? 0 : load);
        }
        return queueLoads;
    }
}

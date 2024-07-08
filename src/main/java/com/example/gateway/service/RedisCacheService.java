package com.example.gateway.service;

public interface RedisCacheService {
    void saveSession(String sessionId, Object session);

    Object getSession(String sessionId);

    void deleteSession(String sessionId);

    Integer getQueueLoad(String queueKey);

    void saveQueueLoad(String queueKey, Integer load);
}

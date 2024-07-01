package com.example.gateway.service;

public interface RedisCacheService {
    void saveSession(String sessionId, Object session);

    Object getSession(String sessionId);

    void deleteSession(String sessionId);
}

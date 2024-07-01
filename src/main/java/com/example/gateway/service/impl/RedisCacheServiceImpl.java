package com.example.gateway.service.impl;

import com.example.gateway.service.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisCacheServiceImpl implements RedisCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SESSION_CACHE_PREFIX = "session:";

    @Override
    public void saveSession(String sessionId, Object session) {
        redisTemplate.opsForValue().set(SESSION_CACHE_PREFIX + sessionId, session);
    }

    @Override
    public Object getSession(String sessionId) {
        return redisTemplate.opsForValue().get(SESSION_CACHE_PREFIX + sessionId);
    }

    @Override
    public void deleteSession(String sessionId) {
        redisTemplate.delete(SESSION_CACHE_PREFIX + sessionId);
    }
}

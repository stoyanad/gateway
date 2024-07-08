package com.example.gateway.service.impl;

import com.example.gateway.service.RedisCacheService;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisCacheServiceImpl implements RedisCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SESSION_CACHE_PREFIX = "session:";
    private static final long CACHE_TTL = 60;
    private final RedisCommands<String, String> redisCommands;

    public RedisCacheServiceImpl(RedisCommands<String, String> redisCommands) {
        this.redisCommands = redisCommands;
    }

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

    @Override
    public Integer getQueueLoad(String queueKey) {
        var loadString = redisCommands.get(queueKey);
        if (loadString != null) {
            return Integer.valueOf(loadString);
        } else {
            return 0;
        }
    }

    @Override
    public void saveQueueLoad(String queueKey, Integer load) {
        redisCommands.set(queueKey, load.toString());
        redisCommands.expire(queueKey, CACHE_TTL);
    }
}

package com.example.gateway.service;

public interface MessageQueueService {

    void sendMessage(String message);

    String fetchMessage(String queueKey);

}

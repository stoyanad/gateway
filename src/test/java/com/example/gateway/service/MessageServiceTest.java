package com.example.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private MessageQueueService messageQueueService;

    @Mock
    private LeastLoadedQueueService leastLoadedQueueService;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MessageService messageService;

    private Map<String, Object> message;

    @BeforeEach
    void setUp() {
        message = new HashMap<>();
        message.put("key", "value");
    }

    @Test
    void sendMessage_shouldSuccess() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(message)).thenReturn("messageString");

        // When
        messageService.sendMessage(message);

        // Then
        verify(messageQueueService).sendMessage("messageString");
        verify(objectMapper).writeValueAsString(message);
    }

    @Test
    void sendMessage_shouldFail() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(message)).thenReturn("messageString");
        doThrow(new RuntimeException("Queue failure")).when(messageQueueService).sendMessage("messageString");

        // When
        assertThrows(Exception.class, () -> messageService.sendMessage(message));

        // Then
        verify(messageQueueService).sendMessage("messageString");
        verify(objectMapper).writeValueAsString(message);
    }

    @Test
    void sendToLeastLoadedQueue_shouldSendMessageToLeastLoadedQueue() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(message)).thenReturn("messageString");
        when(leastLoadedQueueService.determineLeastLoadedQueueAndIncrementLoad()).thenReturn("leastLoadedQueue");

        // When
        messageService.sendToLeastLoadedQueue(message);

        // Then
        verify(leastLoadedQueueService).determineLeastLoadedQueueAndIncrementLoad();
        verify(messageQueueService).sendMessage("messageString");
        verify(objectMapper).writeValueAsString(message);
    }

    @Test
    void sendToLeastLoadedQueue_shouldFail() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(message)).thenReturn("messageString");
        when(leastLoadedQueueService.determineLeastLoadedQueueAndIncrementLoad()).thenReturn("leastLoadedQueue");
        when(redisCacheService.getQueueLoad("leastLoadedQueue")).thenReturn(1);
        doThrow(new RuntimeException("Queue failure")).when(messageQueueService).sendMessage("messageString");

        //When
        messageService.sendToLeastLoadedQueue(message);

        //Then
        verify(leastLoadedQueueService).determineLeastLoadedQueueAndIncrementLoad();
        verify(messageQueueService).sendMessage("messageString");
        verify(objectMapper).writeValueAsString(message);
        verify(redisCacheService).saveQueueLoad("leastLoadedQueue", 0);
    }

    @Test
    void decrementQueueLoad_shouldGetDecrementAndSaveQueueLoad() {
        // Given
        when(redisCacheService.getQueueLoad("queueName")).thenReturn(1);

        //When
        messageService.decrementQueueLoad("queueName");

        //Then
        verify(redisCacheService).getQueueLoad("queueName");
        verify(redisCacheService).saveQueueLoad("queueName", 0);
    }

    @Test
    void fetchMessage_shouldPass() {
        when(messageQueueService.fetchMessage("queueKey")).thenReturn("message");

        String result = messageService.fetchMessage("queueKey");

        verify(messageQueueService).fetchMessage("queueKey");
        assert result.equals("message");
    }
}

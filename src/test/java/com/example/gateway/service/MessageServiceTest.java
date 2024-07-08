package com.example.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class MessageServiceTest {

    @MockBean
    private MessageQueueService messageQueueService;

    @MockBean
    private LeastLoadedQueueService leastLoadedQueueService;

    @MockBean
    private RedisCacheService redisCacheService;

    private MessageService messageService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        messageService = new MessageService(messageQueueService, leastLoadedQueueService, redisCacheService);
    }

    @Test
    public void sendInsertCommandMessage_shouldSendCorrectMessage() throws Exception {
        // Given
        String requestId = "testRequestId";
        long timestamp = System.currentTimeMillis();
        String producerId = "testProducerId";
        String sessionId = "testSessionId";

        // When
        messageService.sendInsertCommandMessage(requestId, timestamp, producerId, sessionId);

        // Then
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageQueueService).sendMessage(messageCaptor.capture());

        Map<String, Object> capturedMessage = new ObjectMapper().readValue(messageCaptor.getValue(), HashMap.class);
        assertEquals("InsertCommand", capturedMessage.get("type"));
        Map<String, Object> data = (Map<String, Object>) capturedMessage.get("data");
        assertEquals(requestId, data.get("requestId"));
        assertEquals(timestamp, data.get("timestamp"));
        assertEquals(producerId, data.get("producerId"));
        assertEquals(sessionId, data.get("sessionId"));
    }

    @Test
    public void sendFindCommandMessage_shouldSendToLeastLoadedQueue() throws Exception {
        // Given
        String requestId = "testRequestId";
        String sessionId = "testSessionId";
        String queueName = "leastLoadedQueue";
        when(leastLoadedQueueService.determineLeastLoadedQueueAndIncrementLoad()).thenReturn(queueName);

        // When
        messageService.sendFindCommandMessage(requestId, sessionId);

        // Then
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageQueueService).sendMessage(messageCaptor.capture());

        Map<String, Object> capturedMessage = new ObjectMapper().readValue(messageCaptor.getValue(), HashMap.class);
        assertEquals("FindCommand", capturedMessage.get("type"));
        assertEquals(queueName, capturedMessage.get("queueName"));
        Map<String, Object> data = (Map<String, Object>) capturedMessage.get("data");
        assertEquals(requestId, data.get("requestId"));
        assertEquals(sessionId, data.get("sessionId"));
    }

    @Test
    public void sendSessionIdsMessage_shouldSendToLeastLoadedQueue() throws Exception {
        // Given
        String userId = "testUserId";
        List<String> sessionIds = List.of("123", "234", "345");
        String queueName = "leastLoadedQueue";
        when(leastLoadedQueueService.determineLeastLoadedQueueAndIncrementLoad()).thenReturn(queueName);

        // When
        messageService.sendSessionIdsMessage(userId, sessionIds);

        // Then
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageQueueService).sendMessage(messageCaptor.capture());

        Map<String, Object> capturedMessage = new ObjectMapper().readValue(messageCaptor.getValue(), HashMap.class);
        assertEquals("SessionIdsMessage", capturedMessage.get("type"));
        assertEquals(queueName, capturedMessage.get("queueName"));
        Map<String, Object> data = (Map<String, Object>) capturedMessage.get("data");
        assertEquals(userId, data.get("userId"));
        assertEquals(sessionIds, data.get("sessionIds"));
    }

    @Test
    public void sendMessage_shouldLogErrorAndThrowExceptionOnFailure() {
        // Given
        Map<String, Object> message = new HashMap<>();
        message.put("testKey", "testValue");

        doThrow(new RuntimeException("Test exception")).when(messageQueueService).sendMessage(any());

        // When
        Exception exception = assertThrows(Exception.class, () -> messageService.sendMessage(message));

        // Then
        assertEquals("Test exception", exception.getMessage());
        verify(messageQueueService).sendMessage(any());
    }

    @Test
    public void sendToLeastLoadedQueue_shouldDecrementQueueLoadOnFailure() {
        // Given
        Map<String, Object> message = new HashMap<>();
        String queueName = "leastLoadedQueue";
        when(leastLoadedQueueService.determineLeastLoadedQueueAndIncrementLoad()).thenReturn(queueName);
        when(redisCacheService.getQueueLoad(queueName)).thenReturn(2);

        doThrow(new RuntimeException("Test exception")).when(messageQueueService).sendMessage(any());

        // When
        try {
            messageService.sendToLeastLoadedQueue(message);
        } catch (Exception ignored) {
        }

        // Then
        verify(redisCacheService).saveQueueLoad(queueName, 1);
    }
}

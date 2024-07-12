package com.example.gateway.component;

import com.example.gateway.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QueueProcessorTest {

    @Mock
    private MessageService messageService;

    @Mock
    private MessageConsumer messageConsumer;

    @InjectMocks
    private QueueProcessor queueProcessor;

    @Value("${rabbitmq.other_internal_service.instances}")
    private int instances = 3;  // You can set this value to match your application.properties

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(queueProcessor, "instances", 3);
    }

    @Test
    void processQueue_shouldProcessMessageAndDecrementQueueLoad() {
        // Given
        when(messageService.fetchMessage(anyString()))
                .thenReturn("message1", "message2", null);  // Simulating messages and an empty queue

        // When
        queueProcessor.processQueue();

        // Then
        for (int i = 1; i <= instances; i++) {
            String queueKey = "queue_instance_" + i;
            verify(messageService).fetchMessage(queueKey);
        }
        verify(messageConsumer, times(2)).processQueueMessage(anyString(), anyString());
        verify(messageService, times(2)).decrementQueueLoad(anyString());
        verifyNoMoreInteractions(messageService, messageConsumer);
    }

    @Test
    void processQueue_shouldNotProcessWhenNoMessages() {
        // Given
        when(messageService.fetchMessage(anyString())).thenReturn(null);

        // When
        queueProcessor.processQueue();

        // Then
        for (int i = 1; i <= instances; i++) {
            String queueKey = "queue_instance_" + i;
            verify(messageService).fetchMessage(queueKey);
        }
        verify(messageConsumer, never()).processQueueMessage(anyString(), anyString());
        verify(messageService, never()).decrementQueueLoad(anyString());
        verifyNoMoreInteractions(messageService, messageConsumer);
    }
}

package com.example.gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class LeastLoadedQueueServiceTest {

    public static final String QUEUE_INSTANCE_1 = "queue_instance_1";
    public static final String QUEUE_INSTANCE_2 = "queue_instance_2";
    public static final String QUEUE_INSTANCE_3 = "queue_instance_3";
    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    @Spy
    private LeastLoadedQueueService leastLoadedQueueService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void determineLeastLoadedQueueAndIncrementLoad_shouldReturnCorrectQueueWithLoadAndIncrementIt() {
        // Given
        leastLoadedQueueService.setInstances(3);

        Map<Integer, Integer> mockQueueLoads = new HashMap<>();
        mockQueueLoads.put(1, 5);
        mockQueueLoads.put(2, 10);
        mockQueueLoads.put(3, 34);
        doReturn(mockQueueLoads).when(leastLoadedQueueService).getQueueLoads();

        when(redisCacheService.getQueueLoad(QUEUE_INSTANCE_1)).thenReturn(5);
        when(redisCacheService.getQueueLoad(QUEUE_INSTANCE_2)).thenReturn(10);
        when(redisCacheService.getQueueLoad(QUEUE_INSTANCE_3)).thenReturn(34);

        doNothing().when(redisCacheService).saveQueueLoad(anyString(), anyInt());

        // When
        String leastLoadedQueue = leastLoadedQueueService.determineLeastLoadedQueueAndIncrementLoad();

        // Then
        assertEquals(QUEUE_INSTANCE_1, leastLoadedQueue);
        verify(redisCacheService, times(1)).saveQueueLoad(QUEUE_INSTANCE_1, 6);
    }

    @Test
    public void determineLeastLoadedQueueAndIncrementLoad_shouldReturnCorrectQueueWithNullLoadAndIncrementIt() {
        // Given
        leastLoadedQueueService.setInstances(3);

        Map<Integer, Integer> mockQueueLoads = new HashMap<>();
        mockQueueLoads.put(1, 5);
        mockQueueLoads.put(2, 10);
        mockQueueLoads.put(3, null);
        doReturn(mockQueueLoads).when(leastLoadedQueueService).getQueueLoads();

        when(redisCacheService.getQueueLoad(QUEUE_INSTANCE_1)).thenReturn(5);
        when(redisCacheService.getQueueLoad(QUEUE_INSTANCE_2)).thenReturn(10);
        when(redisCacheService.getQueueLoad(QUEUE_INSTANCE_3)).thenReturn(null);

        doNothing().when(redisCacheService).saveQueueLoad(anyString(), anyInt());

        // When
        String leastLoadedQueue = leastLoadedQueueService.determineLeastLoadedQueueAndIncrementLoad();

        // Then
        assertEquals(QUEUE_INSTANCE_3, leastLoadedQueue);
        verify(redisCacheService, times(1)).saveQueueLoad(QUEUE_INSTANCE_3, 1);
    }

    @Test
    public void getQueueLoads_shouldReturnCorrectLoad() {
        // Given
        when(redisCacheService.getQueueLoad(QUEUE_INSTANCE_1)).thenReturn(5);
        when(redisCacheService.getQueueLoad(QUEUE_INSTANCE_2)).thenReturn(10);
        when(redisCacheService.getQueueLoad(QUEUE_INSTANCE_3)).thenReturn(null);

        leastLoadedQueueService.setInstances(3);

        // When
        Map<Integer, Integer> queueLoads = leastLoadedQueueService.getQueueLoads();

        // Then
        assertEquals(3, queueLoads.size());
        assertEquals(5, queueLoads.get(1));
        assertEquals(10, queueLoads.get(2));
        assertEquals(0, queueLoads.get(3));
    }

}

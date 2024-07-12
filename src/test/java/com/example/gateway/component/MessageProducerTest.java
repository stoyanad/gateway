package com.example.gateway.component;

import com.example.gateway.service.MessageQueueService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class MessageProducerTest {

    @Mock
    private MessageQueueService messageQueueService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MessageProducer messageProducer = new MessageProducer(messageQueueService, objectMapper);

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void createMessage2() throws Exception{
        // Given
        String userIdtest = "1234";

        //When
        String message = messageProducer.createMessage(userIdtest);

        //Then
        System.out.println(message);
        JsonNode messageNode = objectMapper.readTree(message);

        String id = messageNode.get("id").asText();
        String timestamp = messageNode.get("timestamp").asText();
        String userId = messageNode.get("userId").asText();
        JsonNode requestNode = messageNode.get("request");
        String method = requestNode.get("method").asText();
        String url = requestNode.get("url").asText();

        assertNotNull(id);
        assertNotNull(timestamp);
        assertEquals(userIdtest, userId);
        assertEquals("GET", method);
        assertEquals("/statistics/1234/sessions", url);

    }
}

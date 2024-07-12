package com.example.gateway.component;

import com.example.gateway.repository.ResponseRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class MessageConsumerTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ResponseRepository responseRepository;

    private MessageConsumer messageConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        messageConsumer = new MessageConsumer(responseRepository, webClientBuilder, objectMapper);
    }

    @Test
    void processQueueMessage_shouldPass() throws JsonProcessingException {
        // Given
        String queueKey = "someQueueKey";
        String message = "{\"id\":\"someId\",\"timestamp\":\"someTimestamp\",\"userId\":\"238485\",\"request\":{\"method\":\"GET\",\"url\":\"/statistics/238485/sessions\"}}";
        String requestUrl = "/statistics/238485/sessions";
        String responseContent = "Response content";

        JsonNode jsonNode = mock(JsonNode.class);
        when(objectMapper.readTree(message)).thenReturn(jsonNode);
        when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseContent));

        // When
        messageConsumer.processQueueMessage(message, queueKey);

        // Then
        StepVerifier.create(Mono.just(responseContent))
                .expectNext(responseContent)
                .verifyComplete();

    }

}
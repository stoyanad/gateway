package com.example.gateway.component;

import com.example.gateway.model.ResponseEntity;
import com.example.gateway.repository.ResponseRepository;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
/**
 * MessageConsumer is responsible for consuming messages from the queue,
 * making HTTP requests based on the message data, and saving the responses to the PostgreSQL database.
 */
@Slf4j
@Service
public class MessageConsumer {

    private final ResponseRepository responseRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public MessageConsumer(ResponseRepository responseRepository, WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.responseRepository = responseRepository;
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
        this.objectMapper = objectMapper;
    }

    public void processQueueMessage(String queueKey, String message) {
        try {
            log.debug("Message received: {}", message); // Log the received message

            if (!isValidJson(message)) {
                log.warn("Received message is not valid JSON: {}", message);
                return;
            }
            JsonNode messageNode = objectMapper.readTree(message);

            String requestId = messageNode.get("id").asText();
            JsonNode requestNode = messageNode.get("request");
            String requestUrl = requestNode.get("url").asText();

            webClient.get()
                    .uri(requestUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> saveResponseToDatabase(queueKey, requestId, response),
                            error -> log.error("Failed to process message: {}", error.getMessage())
                    );

        } catch (JsonParseException | JsonMappingException e) {
            log.error("Failed to parse JSON message: {}", message, e);
        } catch (Exception e) {
            log.error("Failed to process queue message: {}", message, e);
        }
    }


    private void saveResponseToDatabase(String queueKey, String requestId, String response) {
        ResponseEntity responseEntity = new ResponseEntity();
        responseEntity.setQueueKey(queueKey);
        responseEntity.setRequestId(requestId);
        responseEntity.setResponse(response);
        responseRepository.save(responseEntity);
        log.info("Saved response to database for requestId {}: {}", requestId, response);
    }

    private boolean isValidJson(String message) {
        try {
            objectMapper.readTree(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}

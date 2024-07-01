package com.example.gateway.service;

import com.example.gateway.model.InsertEnterCommand;
import com.example.gateway.model.FindGetCommand;
import com.example.gateway.model.Request;
import com.example.gateway.model.Session;
import com.example.gateway.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class GatewayServiceTest {

    @MockBean
    private SessionRepository sessionRepository;

    @MockBean
    private MessageService messageService;

    @MockBean
    private RedisCacheService redisCacheService;

    @Autowired
    private GatewayService gatewayService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void processInsertEnterCommand_shouldInsertNewRequest() throws Exception {
        // Given
        String sessionId = "testSessionId";
        String requestId = "testRequestId";
        long timestamp = System.currentTimeMillis();
        String userId = "testUserId";

        InsertEnterCommand command = new InsertEnterCommand();
        command.setSessionId(sessionId);
        command.setRequestId(requestId);
        command.setTimestamp(timestamp);
        command.setUserId(userId);

        Session session = new Session();
        session.setSessionId(sessionId);

        given(sessionRepository.findBySessionId(sessionId)).willReturn(Optional.of(session));

        // When
        gatewayService.processInsertEnterCommand(command);

        // Then
        verify(sessionRepository).save(session);
        verify(messageService).sendInsertCommandMessage(requestId, timestamp, userId, sessionId);
    }

    @Test
    public void processInsertEnterCommand_shouldThrowExceptionForDuplicateRequest() throws Exception {
        // Given
        String sessionId = "testSessionId";
        String requestId = "testRequestId";
        long timestamp = System.currentTimeMillis();
        String userId = "testUserId";

        InsertEnterCommand command = new InsertEnterCommand();
        command.setSessionId(sessionId);
        command.setRequestId(requestId);
        command.setTimestamp(timestamp);
        command.setUserId(userId);

        Session session = new Session();
        session.setSessionId(sessionId);

        Request existingRequest = new Request();
        existingRequest.setRequestId(requestId);

        session.setRequests(Set.of(existingRequest));

        given(sessionRepository.findBySessionId(sessionId)).willReturn(Optional.of(session));

        // When
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gatewayService.processInsertEnterCommand(command);
        });

        // Then
        assertEquals("Duplicate requestId", exception.getMessage());
        verify(messageService, never()).sendInsertCommandMessage(anyString(), anyLong(), anyString(), anyString());
    }

    @Test
    public void processFindGetCommand_shouldReturnRequestIds() throws Exception {
        // Given
        String sessionId = "testSessionId";
        String requestId = "testRequestId";

        FindGetCommand command = new FindGetCommand();
        command.setSessionId(sessionId);
        command.setRequestId(requestId);

        Session session = new Session();
        session.setSessionId(sessionId);

        Request request1 = new Request();
        request1.setRequestId("requestId1");

        Request request2 = new Request();
        request2.setRequestId(requestId);

        Set<Request> requests = Set.of(request1, request2);

        session.setRequests(requests);

        given(sessionRepository.findBySessionId(sessionId)).willReturn(Optional.of(session));
        doNothing().when(messageService).sendFindCommandMessage(requestId, sessionId);

        // When
        List<String> result = gatewayService.processFindGetCommand(command);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("requestId1"));
        assertTrue(result.contains("testRequestId"));
        verify(messageService).sendFindCommandMessage(requestId, sessionId);
    }

    @Test
    public void saveSession_shouldSaveSessionAndCacheIt() {
        // Given
        Session session = new Session();
        session.setSessionId("123");
        when(sessionRepository.save(session)).thenReturn(session);

        // When
        gatewayService.saveSession(session);

        // Then
        verify(sessionRepository).save(session);
        verify(redisCacheService).saveSession(session.getSessionId(), session);
    }

    @Test
    public void findSessionIdsByUserId_shouldReturnSessionIdsAndSendMessage() throws Exception {
        // Given
        String userId = "userId";
        List<String> sessionIdsByUserId = List.of("123", "234", "345");
        given(sessionRepository.findSessionIdsByUserId(userId)).willReturn(sessionIdsByUserId);

        // When
        List<String> foundSessions = gatewayService.findSessionIdsByUserId(userId);

        // Then
        verify(messageService).sendSessionIdsMessage(userId, sessionIdsByUserId);
        assertEquals(sessionIdsByUserId, foundSessions);
    }

    @Test
    public void findSessionBySessionId_shouldReturnSessionFromCache() {
        // Given
        String sessionId = "123";
        Session session = new Session();
        session.setSessionId(sessionId);
        when(redisCacheService.getSession(sessionId)).thenReturn(session);

        // When
        gatewayService.findSessionBySessionId(sessionId);
        gatewayService.findSessionBySessionId(sessionId);
        gatewayService.findSessionBySessionId(sessionId);

        // Then
        verify(redisCacheService, times(3)).getSession(sessionId);
        verify(sessionRepository, times(0)).findBySessionId(sessionId);
    }

    @Test
    public void findSessionBySessionId_shouldReturnSessionFromRepositoryAndSaveToCache() {
        // Given
        String sessionId = "123";
        Session session = new Session();
        session.setSessionId(sessionId);
        given(sessionRepository.findBySessionId(sessionId)).willReturn(Optional.of(session));

        // When
        Optional<Session> result = gatewayService.findSessionBySessionId(sessionId);

        // Then
        verify(sessionRepository, times(1)).findBySessionId(sessionId);
        verify(redisCacheService, times(1)).saveSession(sessionId, session);
    }
}

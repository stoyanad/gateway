package com.example.gateway.service;

import com.example.gateway.model.FindGetCommand;
import com.example.gateway.model.InsertEnterCommand;
import com.example.gateway.model.Request;
import com.example.gateway.model.Session;
import com.example.gateway.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class SessionServiceTest {

    @MockBean
    private SessionRepository sessionRepository;

    @MockBean
    private RedisCacheService redisCacheService;

    private SessionService sessionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        sessionService = new SessionService(sessionRepository, redisCacheService);
    }

    @Test
    public void handleInsertEnterCommand_shouldInsertNewRequest() {
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
        sessionService.handleInsertEnterCommand(command);

        // Then
        verify(sessionRepository).save(session);
    }

    @Test
    public void handleInsertEnterCommand_shouldThrowExceptionForDuplicateRequest() {
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
            sessionService.handleInsertEnterCommand(command);
        });

        // Then
        assertEquals("Duplicate requestId", exception.getMessage());
    }

    @Test
    public void handleFindGetCommand_shouldReturnRequestIds() {
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

        session.setRequests(Set.of(request1, request2));

        given(sessionRepository.findBySessionId(sessionId)).willReturn(Optional.of(session));

        // When
        List<String> result = sessionService.handleFindGetCommand(command);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("requestId1"));
        assertTrue(result.contains("testRequestId"));
    }

    @Test
    public void handleFindSessionIdsByUserId_shouldReturnSessionIds() {
        // Given
        String userId = "userId";
        List<String> sessionIdsByUserId = List.of("123", "234", "345");
        given(sessionRepository.findSessionIdsByUserId(userId)).willReturn(sessionIdsByUserId);

        // When
        List<String> foundSessions = sessionService.handleFindSessionIdsByUserId(userId);

        // Then
        assertEquals(sessionIdsByUserId, foundSessions);
    }

    @Test
    public void saveSession_shouldSaveSessionAndCacheIt() {
        // Given
        Session session = new Session();
        session.setSessionId("123");
        when(sessionRepository.save(session)).thenReturn(session);

        // When
        sessionService.saveSession(session);

        // Then
        verify(sessionRepository).save(session);
        verify(redisCacheService).saveSession(session.getSessionId(), session);
    }

    @Test
    public void findSessionBySessionId_shouldReturnSessionFromCache() {
        // Given
        String sessionId = "123";
        Session session = new Session();
        session.setSessionId(sessionId);
        when(redisCacheService.getSession(sessionId)).thenReturn(session);

        // When
        sessionService.findSessionBySessionId(sessionId);
        sessionService.findSessionBySessionId(sessionId);
        sessionService.findSessionBySessionId(sessionId);

        // Then
        verify(redisCacheService, times(3)).getSession(sessionId);
        verify(sessionRepository, never()).findBySessionId(sessionId);
    }

    @Test
    public void findSessionBySessionId_shouldReturnSessionFromRepositoryAndSaveToCache() {
        // Given
        String sessionId = "123";
        Session session = new Session();
        session.setSessionId(sessionId);
        given(sessionRepository.findBySessionId(sessionId)).willReturn(Optional.of(session));

        // When
        Optional<Session> result = sessionService.findSessionBySessionId(sessionId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(sessionId, result.get().getSessionId());
        verify(sessionRepository).findBySessionId(sessionId);
        verify(redisCacheService).saveSession(sessionId, session);
    }
}

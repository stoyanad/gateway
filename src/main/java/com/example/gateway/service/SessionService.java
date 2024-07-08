package com.example.gateway.service;

import com.example.gateway.model.FindGetCommand;
import com.example.gateway.model.InsertEnterCommand;
import com.example.gateway.model.Session;
import com.example.gateway.model.Request;
import com.example.gateway.repository.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final RedisCacheService redisCacheService;

    @Autowired
    public SessionService(SessionRepository sessionRepository, RedisCacheService redisCacheService) {
        this.sessionRepository = sessionRepository;
        this.redisCacheService = redisCacheService;
    }

    public void handleInsertEnterCommand(InsertEnterCommand command) {
        Session session = findOrCreateSession(command.getSessionId());

        if (session.getRequests().stream().noneMatch(r -> r.getRequestId().equals(command.getRequestId()))) {
            Request request = new Request();
            request.setRequestId(command.getRequestId());
            request.setTimestamp(command.getTimestamp());
            request.setSession(session);
            session.addRequest(request);
            saveSession(session);

            log.info("Request ID {} added to session ID {}", command.getRequestId(), command.getSessionId());
        } else {
            log.warn("Duplicate requestId {} found for sessionId {}", command.getRequestId(), command.getSessionId());
            throw new IllegalArgumentException("Duplicate requestId");
        }
    }

    public List<String> handleFindGetCommand(FindGetCommand command) {
        Session session = findOrCreateSession(command.getSessionId());

        List<String> requestIds = session.getRequests().stream()
                .map(Request::getRequestId)
                .collect(Collectors.toList());

        log.info("Found {} request IDs for session ID {}", requestIds.size(), command.getSessionId());

        return requestIds;
    }

    public List<String> handleFindSessionIdsByUserId(String userId) {
        List<String> sessionIdsByUserId = sessionRepository.findSessionIdsByUserId(userId);
        log.info("Found {} session IDs for userId: {}", sessionIdsByUserId.size(), userId);

        return sessionIdsByUserId;
    }

    private Session findOrCreateSession(String sessionId) {
        return findSessionBySessionId(sessionId)
                .orElseGet(() -> createNewSession(sessionId));
    }

    private Session createNewSession(String sessionId) {
        log.info("Creating new session for sessionId: {}", sessionId);
        Session session = new Session();
        session.setSessionId(sessionId);
        return saveSession(session);
    }

    Session saveSession(Session session) {
        log.debug("Saving session with sessionId: {}", session.getSessionId());
        Session savedSession = sessionRepository.save(session);
        redisCacheService.saveSession(session.getSessionId(), savedSession);
        log.debug("Session saved with sessionId: {}", session.getSessionId());
        return savedSession;
    }

    Optional<Session> findSessionBySessionId(String sessionId) {
        log.debug("Searching for session with sessionId: {} in Redis cache", sessionId);
        Optional<Session> sessionOptional = Optional.ofNullable((Session) redisCacheService.getSession(sessionId));
        if (sessionOptional.isEmpty()) {
            log.debug("Session not found in Redis cache for sessionId: {}. Searching in repository.", sessionId);
            sessionOptional = sessionRepository.findBySessionId(sessionId);
            sessionOptional.ifPresent(s -> redisCacheService.saveSession(sessionId, s));
        } else {
            log.debug("Session found in Redis cache for sessionId: {}", sessionId);
        }
        return sessionOptional;
    }
}

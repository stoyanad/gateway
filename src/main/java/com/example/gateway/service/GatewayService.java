package com.example.gateway.service;

import com.example.gateway.model.FindGetCommand;
import com.example.gateway.model.InsertEnterCommand;
import com.example.gateway.model.Session;
import com.example.gateway.model.Request;
import com.example.gateway.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GatewayService {

    private final SessionRepository sessionRepository;
    private final MessageService messageService;
    private final RedisCacheService redisCacheService;

    private static final Logger logger = LoggerFactory.getLogger(GatewayService.class);

    @Autowired
    public GatewayService(SessionRepository sessionRepository, MessageService messageService, RedisCacheService redisCacheService) {
        this.sessionRepository = sessionRepository;
        this.messageService = messageService;
        this.redisCacheService = redisCacheService;
    }

    /**
     * Checks if the session exists, checks if the requestId already exists for this session,
     * processes the command and saves the session
     */
    public void processInsertEnterCommand(InsertEnterCommand command) throws Exception {
        logger.info("Processing InsertEnterCommand for sessionId: {}", command.getSessionId());

        Session session = findSessionBySessionId(command.getSessionId())
                .orElseGet(() -> createNewSession(command.getSessionId()));

        if (session.getRequests().stream().noneMatch(r -> r.getRequestId().equals(command.getRequestId()))) {
            logger.debug("Request ID {} not found in session. Creating new request.", command.getRequestId());

            var request = new Request();
            request.setRequestId(command.getRequestId());
            request.setTimestamp(command.getTimestamp());
            request.setSession(session);
            session.addRequest(request);
            saveSession(session);

            logger.info("Request ID {} added to session ID {}", command.getRequestId(), command.getSessionId());
            messageService.sendInsertCommandMessage(command.getRequestId(), command.getTimestamp(), command.getUserId(), command.getSessionId());
        } else {
            logger.warn("Duplicate requestId {} found for sessionId {}", command.getRequestId(), command.getSessionId());
            throw new IllegalArgumentException("Duplicate requestId");
        }
    }

    /**
     * Checks if the session exists and collects and returns all requestIds associated with this session
     */
    public List<String> processFindGetCommand(FindGetCommand command) throws Exception {
        logger.info("Processing FindGetCommand for sessionId: {}", command.getSessionId());

        var session = findSessionBySessionId(command.getSessionId())
                .orElseGet(() -> createNewSession(command.getSessionId()));

        List<String> requestIds = session.getRequests().stream()
                .map(Request::getRequestId)
                .collect(Collectors.toList());

        logger.info("Found {} request IDs for session ID {}", requestIds.size(), command.getSessionId());
        messageService.sendFindCommandMessage(command.getRequestId(), command.getSessionId());

        return requestIds;
    }

    Session createNewSession(String sessionId) {
        logger.info("Creating new session for sessionId: {}", sessionId);
        var session = new Session();
        session.setSessionId(sessionId);
        return saveSession(session);
    }

    Session saveSession(Session session) {
        logger.debug("Saving session with sessionId: {}", session.getSessionId());
        var savedSession = sessionRepository.save(session);
        redisCacheService.saveSession(session.getSessionId(), savedSession);
        logger.debug("Session saved with sessionId: {}", session.getSessionId());
        return savedSession;
    }

    public List<String> findSessionIdsByUserId(String userId) throws Exception {
        logger.info("Finding session IDs for userId: {}", userId);
        List<String> sessionIdsByUserId = sessionRepository.findSessionIdsByUserId(userId);
        logger.info("Found {} session IDs for userId: {}", sessionIdsByUserId.size(), userId);
        messageService.sendSessionIdsMessage(userId, sessionIdsByUserId);
        return sessionIdsByUserId;
    }

    public Optional<Session> findSessionBySessionId(String sessionId) {
        logger.debug("Searching for session with sessionId: {} in Redis cache", sessionId);
        var session = (Session) redisCacheService.getSession(sessionId);
        if (session != null) {
            logger.debug("Session found in Redis cache for sessionId: {}", sessionId);
            return Optional.of(session);
        }

        logger.debug("Session not found in Redis cache for sessionId: {}. Searching in repository.", sessionId);
        Optional<Session> sessionOptional = sessionRepository.findBySessionId(sessionId);
        sessionOptional.ifPresent(s -> {
            logger.debug("Session found in repository for sessionId: {}. Saving to Redis cache.", sessionId);
            redisCacheService.saveSession(sessionId, s);
        });

        return sessionOptional;
    }
}

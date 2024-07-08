package com.example.gateway.service;

import com.example.gateway.model.FindGetCommand;
import com.example.gateway.model.InsertEnterCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GatewayService {

    private final SessionService sessionService;
    private final MessageService messageService;

    @Autowired
    public GatewayService(SessionService sessionService, MessageService messageService) {
        this.sessionService = sessionService;
        this.messageService = messageService;
    }

    public void processInsertEnterCommand(InsertEnterCommand command) throws Exception {
        log.info("Processing InsertEnterCommand for sessionId: {}", command.getSessionId());

        sessionService.handleInsertEnterCommand(command);
        messageService.sendInsertCommandMessage(command.getRequestId(), command.getTimestamp(), command.getUserId(), command.getSessionId());
    }

    public List<String> processFindGetCommand(FindGetCommand command) {
        log.info("Processing FindGetCommand for sessionId: {}", command.getSessionId());

        List<String> requestIds = sessionService.handleFindGetCommand(command);
        messageService.sendFindCommandMessage(command.getRequestId(), command.getSessionId());

        return requestIds;
    }

    public List<String> findSessionIdsByUserId(String userId) {
        log.info("Finding session IDs for userId: {}", userId);

        List<String> sessionIdsByUserId = sessionService.handleFindSessionIdsByUserId(userId);
        messageService.sendSessionIdsMessage(userId, sessionIdsByUserId);

        return sessionIdsByUserId;
    }
}

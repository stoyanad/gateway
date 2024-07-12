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

    @Autowired
    public GatewayService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public void processInsertEnterCommand(InsertEnterCommand command) {
        log.info("Processing InsertEnterCommand for sessionId: {}", command.getSessionId());

        sessionService.handleInsertEnterCommand(command);
    }

    public List<String> processFindGetCommand(FindGetCommand command) {
        log.info("Processing FindGetCommand for sessionId: {}", command.getSessionId());

        return sessionService.handleFindGetCommand(command);
    }

    public List<String> findSessionIdsByUserId(String userId) {
        log.info("Finding session IDs for userId: {}", userId);

        return sessionService.handleFindSessionIdsByUserId(userId);
    }
}

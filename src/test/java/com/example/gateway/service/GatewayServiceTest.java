package com.example.gateway.service;

import com.example.gateway.model.FindGetCommand;
import com.example.gateway.model.InsertEnterCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class GatewayServiceTest {

    @MockBean
    private SessionService sessionService;

    @MockBean
    private MessageService messageService;

    private GatewayService gatewayService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        gatewayService = new GatewayService(sessionService, messageService);
    }

    @Test
    public void processInsertEnterCommand_shouldDelegateToSessionServiceAndSendMessage() throws Exception {
        // Given
        InsertEnterCommand command = new InsertEnterCommand();
        command.setSessionId("testSessionId");
        command.setRequestId("testRequestId");
        command.setTimestamp(System.currentTimeMillis());
        command.setUserId("testUserId");

        // When
        gatewayService.processInsertEnterCommand(command);

        // Then
        verify(sessionService).handleInsertEnterCommand(command);
        verify(messageService).sendInsertCommandMessage(command.getRequestId(), command.getTimestamp(), command.getUserId(), command.getSessionId());
    }

    @Test
    public void processFindGetCommand_shouldDelegateToSessionServiceAndSendMessage() throws Exception {
        // Given
        FindGetCommand command = new FindGetCommand();
        command.setSessionId("testSessionId");
        command.setRequestId("testRequestId");

        // When
        gatewayService.processFindGetCommand(command);

        // Then
        verify(sessionService).handleFindGetCommand(command);
        verify(messageService).sendFindCommandMessage(command.getRequestId(), command.getSessionId());
    }

    @Test
    public void findSessionIdsByUserId_shouldDelegateToSessionServiceAndSendMessage() throws Exception {
        // Given
        String userId = "testUserId";

        // When
        gatewayService.findSessionIdsByUserId(userId);

        // Then
        verify(sessionService).handleFindSessionIdsByUserId(userId);
        verify(messageService).sendSessionIdsMessage(eq(userId), any());
    }
}

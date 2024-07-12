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

    private GatewayService gatewayService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        gatewayService = new GatewayService(sessionService);
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
    }

    @Test
    public void processFindGetCommand_shouldDelegateToSessionServiceAndSendMessage() {
        // Given
        FindGetCommand command = new FindGetCommand();
        command.setSessionId("testSessionId");
        command.setRequestId("testRequestId");

        // When
        gatewayService.processFindGetCommand(command);

        // Then
        verify(sessionService).handleFindGetCommand(command);
    }

    @Test
    public void findSessionIdsByUserId_shouldDelegateToSessionServiceAndSendMessage() {
        // Given
        String userId = "testUserId";

        // When
        gatewayService.findSessionIdsByUserId(userId);

        // Then
        verify(sessionService).handleFindSessionIdsByUserId(userId);
    }
}

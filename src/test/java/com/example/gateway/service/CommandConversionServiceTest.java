package com.example.gateway.service;

import com.example.gateway.dto.CommandXmlDto;
import com.example.gateway.dto.EnterCommand;
import com.example.gateway.dto.GetCommand;
import com.example.gateway.model.FindGetCommand;
import com.example.gateway.model.InsertEnterCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandConversionServiceTest {

    public static final long TIMESTAMP = 1586335186721L;
    public static final String USER = "player1";
    public static final String SESSION = "session1";
    public static final String ID = "12345";
    private CommandConversionService commandConversionService;

    @BeforeEach
    void setUp() {
        commandConversionService = new CommandConversionService();
    }

    @Test
    void convertEnter_shouldConvertToCorrectDto() {
        //Given
        CommandXmlDto xmlCommand = new CommandXmlDto();
        xmlCommand.setId(ID);

        EnterCommand enterCommand = new EnterCommand();
        enterCommand.setTimestamp(TIMESTAMP);
        enterCommand.setPlayer(USER);
        enterCommand.setSession(SESSION);

        xmlCommand.setEnter(enterCommand);

        //When
        InsertEnterCommand result = commandConversionService.convertEnter(xmlCommand);

        //Then
        assertNotNull(result);
        assertEquals(ID, result.getRequestId());
        assertEquals(TIMESTAMP, result.getTimestamp());
        assertEquals(USER, result.getUserId());
        assertEquals(SESSION, result.getSessionId());
    }

    @Test
    void convertGet_shouldConvertToCorrectDto() {
        //Given
        CommandXmlDto xmlCommand = new CommandXmlDto();
        xmlCommand.setId("67890");

        GetCommand getCommand = new GetCommand();
        getCommand.setSession("session2");

        xmlCommand.setGet(getCommand);

        //When
        FindGetCommand result = commandConversionService.convertGet(xmlCommand);

        //Then
        assertNotNull(result);
        assertEquals("67890", result.getRequestId());
        assertEquals("session2", result.getSessionId());
    }
}

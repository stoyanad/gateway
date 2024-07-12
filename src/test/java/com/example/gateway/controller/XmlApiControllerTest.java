package com.example.gateway.controller;

import com.example.gateway.service.CommandConversionService;
import com.example.gateway.service.GatewayService;
import jakarta.xml.bind.JAXBException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(XmlApiController.class)
public class XmlApiControllerTest {

    public static final String ENTER_REQUEST = """

            <command id="1234">
                <enter session="13617162">
                    <timestamp>1586335186721</timestamp>
                    <player>238485</player>
                </enter>
            </command>""";

    public static final String GET_REQUEST = """

            <command id="1234-8785">
               <get session="13617162" />
            </command>""";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GatewayService gatewayService;

    @MockBean
    private CommandConversionService commandConversionService;

    @Autowired
    private XmlApiController xmlApiController;

    @Test
    public void processXmlCommandShouldReturnOkForEnterCommand() throws Exception {
        mockMvc.perform(post("/xml_api/command")
                        .content(ENTER_REQUEST)
                        .contentType(MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_JSON)) // Expecting JSON response
                .andExpect(status().isOk());
    }

    @Test
    public void processXmlCommandShouldReturnOkForGetCommand() throws Exception {
        List<String> requestIds = List.of("b89577fe-8c37-4962-8af3-7cb89a245160");

        doReturn(requestIds).when(gatewayService).processFindGetCommand(any());

        mockMvc.perform(post("/xml_api/command")
                        .content(GET_REQUEST)
                        .contentType(MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_JSON)) // Expecting JSON response
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]", equalTo("b89577fe-8c37-4962-8af3-7cb89a245160")));
    }

    @Test
    public void processXmlCommandShouldReturnInternalServerError() throws Exception {
        doThrow(RuntimeException.class).when(gatewayService).processInsertEnterCommand(any());

        mockMvc.perform(post("/xml_api/command")
                        .content(ENTER_REQUEST)
                        .contentType(MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void parseRequestShouldReturnCorrectEnterDto() throws JAXBException {
        // when
        var commandXmlDto = xmlApiController.parseRequest(ENTER_REQUEST);

        // then
        assertEquals("1234", commandXmlDto.getId());
        assertEquals("13617162", commandXmlDto.getEnter().getSession());
        assertEquals("238485", commandXmlDto.getEnter().getPlayer());
        assertEquals("1586335186721", commandXmlDto.getEnter().getTimestamp().toString());
    }

    @Test
    public void parseRequestShouldReturnCorrectGetDto() throws JAXBException {
        // when
        var commandXmlDto = xmlApiController.parseRequest(GET_REQUEST);

        // then
        assertEquals("1234-8785", commandXmlDto.getId());
        assertEquals("13617162", commandXmlDto.getGet().getSession());
    }
}
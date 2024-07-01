package com.example.gateway.controller;

import com.example.gateway.dto.RequestJsonDto;
import com.example.gateway.model.Request;
import com.example.gateway.model.Session;
import com.example.gateway.repository.RequestRepository;
import com.example.gateway.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebMvcTest(JsonApiController.class)
class JsonApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionRepository sessionRepository;

    @MockBean
    private RequestRepository requestRepository;

    private RequestJsonDto requestJsonDto;
    Long time = System.currentTimeMillis();

    @BeforeEach
    void setUp() {
        requestJsonDto = new RequestJsonDto();
        requestJsonDto.setSessionId("session123");
        requestJsonDto.setProducerId("user456");
        requestJsonDto.setRequestId("request789");
        requestJsonDto.setTimestamp(time);
    }

    private static final String JSON_REQUEST = "{\n" +
            "\"requestId\": \"b89577fe-8c37-4962-8af3-7cb89a245160\",\n" +
            "\"timestamp\": 1586335186721,\n" +
            "\"producerId\": \"1234\",\n" +
            "\"sessionId\": 47966003032113150\n" +
            "}";

    @Test
    void insertCommand_shouldReturnOk() throws Exception {
        when(sessionRepository.findBySessionId("session123")).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/json_api/insert")
                        .content(JSON_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("OK"));
    }

    @Test
    void findCommand_shouldReturnRequestIds() throws Exception {
        Session session = new Session();
        session.setSessionId("session123");

        Request request = new Request();
        request.setRequestId("request789");

        session.setRequests(Set.of(request));

        when(sessionRepository.findBySessionId("session123")).thenReturn(Optional.of(session));

        mockMvc.perform(MockMvcRequestBuilders.post("/json_api/find")
                        .content("{\"sessionId\":\"session123\"}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value("request789"));
    }
}

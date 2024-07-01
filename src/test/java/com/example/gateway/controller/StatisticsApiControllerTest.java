package com.example.gateway.controller;

import com.example.gateway.service.GatewayService;
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

import java.util.Collections;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebMvcTest(StatisticsApiController.class)
class StatisticsApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GatewayService gatewayService;

    @Test
    void getUserSessions_shouldReturnSessionIds_whenFound() throws Exception {
        String userId = "123";

        when(gatewayService.findSessionIdsByUserId(userId)).thenReturn(Collections.singletonList("session123"));

        mockMvc.perform(MockMvcRequestBuilders.get("/statistics/{userId}/sessions", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value("session123"));
    }

    @Test
    void getUserSessions_shouldReturnNotFound_whenEmpty() throws Exception {
        String userId = "456";

        when(gatewayService.findSessionIdsByUserId(userId)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/statistics/{userId}/sessions", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}

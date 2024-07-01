package com.example.gateway.controller;

import com.example.gateway.service.GatewayService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/statistics")
public class StatisticsApiController {

    @Autowired
    private GatewayService gatewayService;

    @Operation(summary = "Retrieve session IDs by user ID")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of session IDs returned successfully"),
            @ApiResponse(code = 404, message = "User ID not found or no sessions found for user")
    })
    @GetMapping("/{userId}/sessions")
    public ResponseEntity<?> getUserSessions(@PathVariable String userId) throws Exception {
        List<String> sessionIds = gatewayService.findSessionIdsByUserId(userId);

        if (sessionIds.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(sessionIds);
    }
}

package com.example.gateway.controller;

import com.example.gateway.dto.RequestJsonDto;
import com.example.gateway.model.Request;
import com.example.gateway.model.Session;
import com.example.gateway.repository.RequestRepository;
import com.example.gateway.repository.SessionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/json_api")
@Tag(name = "JSON API", description = "API for JSON-based requests")
public class JsonApiController {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private RequestRepository requestRepository;

    @PostMapping("/insert")
    @Operation(summary = "Insert a new command", description = "Insert a new command associated with a session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully inserted command"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public String insertCommand(@RequestBody RequestJsonDto command) {
        Optional<Session> optionalSession = sessionRepository.findBySessionId(command.getSessionId());

        Session session;
        if (optionalSession.isEmpty()) {
            // Create new session if it doesn't exist
            session = new Session();
            session.setSessionId(command.getSessionId());
            session.setUserId(command.getProducerId());
            sessionRepository.save(session);
        } else {
            // Use existing session
            session = optionalSession.get();
        }

        // Create new request and associate with session
        var request = new Request();
        request.setRequestId(command.getRequestId());
        request.setTimestamp(command.getTimestamp());
        request.setSession(session);
        requestRepository.save(request);

        return "OK";
    }

    @PostMapping("/find")
    @Operation(summary = "Find commands by session ID", description = "Find all commands associated with a session ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully found commands", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public List<String> findCommand(@RequestBody RequestJsonDto command) {
        Optional<Session> optionalSession = sessionRepository.findBySessionId(command.getSessionId());

        if (optionalSession.isEmpty()) {
            // Handle no sessions found
            throw new RuntimeException("No sessions found with the sessionId: " + command.getSessionId());
        }

        // Collect all requestIds from the found session
        return optionalSession.get().getRequests().stream()
                .map(Request::getRequestId)
                .collect(Collectors.toList());
    }
}

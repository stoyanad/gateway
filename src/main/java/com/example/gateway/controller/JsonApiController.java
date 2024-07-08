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
    @Operation(summary = "Insert a new command", description = "Create new session if it doesn't exist.Create new request and associate with session.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully inserted request"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public String insertCommand(@RequestBody RequestJsonDto command) {
        Optional<Session> optionalSession = sessionRepository.findBySessionId(command.getSessionId());

        Session session;
        if (optionalSession.isEmpty()) {
            session = new Session();
            session.setSessionId(command.getSessionId());
            session.setUserId(command.getProducerId());
            sessionRepository.save(session);
        } else {
            session = optionalSession.get();
        }

        var request = new Request();
        request.setRequestId(command.getRequestId());
        request.setTimestamp(command.getTimestamp());
        request.setSession(session);
        requestRepository.save(request);

        return "OK";
    }

    @PostMapping("/find")
    @Operation(summary = "Find commands by session ID", description = "Find all requests associated with a session ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully found commands", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public List<String> findCommand(@RequestBody RequestJsonDto command) {
        Optional<Session> optionalSession = sessionRepository.findBySessionId(command.getSessionId());

        if (optionalSession.isEmpty()) {
            throw new RuntimeException("No sessions found with the sessionId: " + command.getSessionId());
        }

        return optionalSession.get().getRequests().stream()
                .map(Request::getRequestId)
                .collect(Collectors.toList());
    }
}

package com.example.gateway.model;

import lombok.Data;

@Data
public class FindGetCommand {
    private String requestId;
    private String sessionId;
}

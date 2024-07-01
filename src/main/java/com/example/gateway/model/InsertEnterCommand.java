package com.example.gateway.model;

import lombok.Data;

@Data
public class InsertEnterCommand {
    private String requestId;
    private Long timestamp;
    private String userId;
    private String sessionId;
}

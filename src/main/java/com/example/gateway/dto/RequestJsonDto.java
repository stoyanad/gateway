package com.example.gateway.dto;

import lombok.Data;

@Data
public class RequestJsonDto {
    private String requestId;
    private Long timestamp;
    private String producerId;
    private String sessionId;
}

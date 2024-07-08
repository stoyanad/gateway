package com.example.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OtherInternalService {

    private final RestTemplate restTemplate;

    @Autowired
    public OtherInternalService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String sendRequest() {
        String url = "http://localhost:8080/statistics/1234/sessions";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        return response.getBody();
    }
}

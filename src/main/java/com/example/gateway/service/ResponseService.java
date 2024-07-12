package com.example.gateway.service;

import com.example.gateway.model.ResponseEntity;
import com.example.gateway.repository.ResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResponseService {

    private final ResponseRepository responseRepository;

    @Autowired
    public ResponseService(ResponseRepository responseRepository) {
        this.responseRepository = responseRepository;
    }

    public void saveResponse(String queueKey, String requestId, String response) {
        ResponseEntity responseEntity = new ResponseEntity();
        responseEntity.setQueueKey(queueKey);
        responseEntity.setRequestId(requestId);
        responseEntity.setResponse(response);

        responseRepository.save(responseEntity);
    }
}

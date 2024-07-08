package com.example.gateway.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class ResponseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String queueKey;

    @Lob
    private String response;

}

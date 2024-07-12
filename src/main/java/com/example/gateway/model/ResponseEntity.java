package com.example.gateway.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
public class ResponseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String queueKey;

    @Column(nullable = false)
    private String requestId;

    @Lob
    @Column(nullable = false)
    private String response;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdAt = new java.util.Date();

}

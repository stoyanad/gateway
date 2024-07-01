package com.example.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.gateway.model.Request;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
}

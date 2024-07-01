package com.example.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.gateway.model.Session;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findBySessionId(String sessionId);

    @Query("SELECT s.sessionId FROM Session s WHERE s.userId = :userId")
    List<String> findSessionIdsByUserId(String userId);
}

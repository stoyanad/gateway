package com.example.gateway.repository;

import com.example.gateway.model.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.BDDAssertions.then;

@DataJpaTest
class SessionRepositoryTest {
    public static final String SESSION_ID = "abcde";
    public static final String USER_ID = "user123";

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void testFindBySessionId_returnsSession() {
        // given
        Session testSession = new Session();
        testSession.setSessionId(SESSION_ID);
        Session savedSession = testEntityManager.persistFlushFind(testSession);

        // when
        Optional<Session> foundSessionOptional = sessionRepository.findBySessionId(SESSION_ID);

        //then
        then(foundSessionOptional).isPresent();
        Session foundSession = foundSessionOptional.get();
        then(foundSession.getId()).isNotNull();
        then(foundSession.getSessionId()).isEqualTo(savedSession.getSessionId());
    }

    @Test
    void testFindSessionIdsByUserId_returnsSessionIds() {
        // given
        Session testSession1 = new Session();
        testSession1.setSessionId("session1");
        testSession1.setUserId(USER_ID);

        Session testSession2 = new Session();
        testSession2.setSessionId("session2");
        testSession2.setUserId(USER_ID);

        testEntityManager.persistFlushFind(testSession1);
        testEntityManager.persistFlushFind(testSession2);

        // when
        List<String> sessionIds = sessionRepository.findSessionIdsByUserId(USER_ID);

        // then
        then(sessionIds).isNotEmpty();
        then(sessionIds).containsExactlyInAnyOrder(testSession1.getSessionId(), testSession2.getSessionId());
    }

}
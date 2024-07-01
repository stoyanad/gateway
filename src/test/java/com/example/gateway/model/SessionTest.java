package com.example.gateway.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SessionTest {

    public static final String SESSION_ID = "sessionId";
    public static final long ID = 1L;
    public static final String USER_ID = "userId";
    public static final Set<Request> requests = Set.of(mock(Request.class), mock(Request.class));

    @Test
    void testConstructorAndGetters() {

        Session session = new Session();
        session.setSessionId(SESSION_ID);
        session.setId(ID);
        session.setRequests(requests);
        session.setUserId(USER_ID);

        assertEquals(SESSION_ID, session.getSessionId());
        assertEquals(ID, session.getId());
        assertEquals(USER_ID, session.getUserId());
        assertEquals(requests, session.getRequests());
    }

    @Test
    void testEqualsAndHashCode() {

        Session session1 = new Session();
        session1.setSessionId(SESSION_ID);
        session1.setId(ID);
        session1.setRequests(requests);
        session1.setUserId(USER_ID);

        Session session2 = new Session();
        session2.setSessionId(SESSION_ID);
        session2.setId(ID);
        session2.setRequests(requests);
        session2.setUserId(USER_ID);

        assertEquals(session1, session2);
        assertEquals(session1.hashCode(), session2.hashCode());
    }
}
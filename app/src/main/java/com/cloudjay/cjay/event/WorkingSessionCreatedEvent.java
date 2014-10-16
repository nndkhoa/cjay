package com.cloudjay.cjay.event;

import com.cloudjay.cjay.model.Session;

import java.util.List;

/**
 * Created by thai on 10/10/2014.
 */
public class WorkingSessionCreatedEvent {
    public Session getWorkingSession() {
        return workingSession;
    }

    public void setworkingSession(Session workingId) {
        this.workingSession = workingId;
    }

    Session workingSession;

    public WorkingSessionCreatedEvent(Session current) {
        this.workingSession = current;
    }
}

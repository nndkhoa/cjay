package com.cloudjay.cjay.model;

import java.util.List;

/**
 * Created by thai on 10/10/2014.
 */
public class WorkingSession {
    public List<Session> getWorkingSession() {
        return workingSession;
    }

    public void setWorkingSession(List<Session> workingID) {
        this.workingSession = workingID;
    }

    List<Session> workingSession;
}

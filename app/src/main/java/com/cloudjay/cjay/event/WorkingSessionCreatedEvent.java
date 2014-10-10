package com.cloudjay.cjay.event;

import com.cloudjay.cjay.model.Session;

import java.util.List;

/**
 * Created by thai on 10/10/2014.
 */
public class WorkingSessionCreatedEvent {
    public List<Session> getListWorkingId() {
        return listWorkingId;
    }

    public void setListWorkingId(List<Session> listWorkingId) {
        this.listWorkingId = listWorkingId;
    }

    List<Session> listWorkingId;

    public WorkingSessionCreatedEvent(List<Session> current) {
        this.listWorkingId = current;
    }
}

package com.cloudjay.cjay.event.session;

import com.cloudjay.cjay.model.Session;

/**
 * Created by nambv on 2015/01/22.
 */
public class ContainerGotParentFragmentEvent {

    private Session session;

    public ContainerGotParentFragmentEvent(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

}

package com.cloudjay.cjay.event;

/**
 * Created by nambv on 14/10/2014.
 */
public class BeginSearchOnServerEvent {
    public String beginSearchOnServer;

    public String getStringEvent() {
        return beginSearchOnServer;
    }

    public BeginSearchOnServerEvent(String beginSearchOnServer) {
        this.beginSearchOnServer = beginSearchOnServer;
    }
}

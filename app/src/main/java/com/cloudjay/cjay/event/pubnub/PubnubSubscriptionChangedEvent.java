package com.cloudjay.cjay.event.pubnub;

/**
 * Created by nambv on 2014/11/24.
 */
public class PubnubSubscriptionChangedEvent {
    private boolean isSubscribed;

    public PubnubSubscriptionChangedEvent(boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
    }

    public boolean isSubscribed() {
        return this.isSubscribed;
    }
}

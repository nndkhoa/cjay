package com.cloudjay.cjay.event;

import com.cloudjay.cjay.model.GateImage;

import io.realm.RealmResults;

/**
 * Created by nambv on 07/10/2014.
 */
public class GateImagesGotEvent {
    private RealmResults<GateImage> gateImages;

    public GateImagesGotEvent(RealmResults<GateImage> gateImages) {
        this.gateImages = gateImages;
    }

    public RealmResults<GateImage> getGateImages() {
        return this.gateImages;
    }
}

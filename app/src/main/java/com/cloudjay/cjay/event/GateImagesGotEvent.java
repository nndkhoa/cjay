package com.cloudjay.cjay.event;

import com.cloudjay.cjay.model.GateImage;

import java.util.List;

/**
 * Created by nambv on 15/10/2014.
 */
public class GateImagesGotEvent {
    private List<GateImage> gateImages;

    public GateImagesGotEvent(List<GateImage> gateImages) {
        this.gateImages = gateImages;
    }

    public List<GateImage> getGateImages() {
        return this.gateImages;
    }
}

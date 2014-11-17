package com.cloudjay.cjay.event.image;

import java.util.ArrayList;

/**
 * Created by nambv on 2014/11/17.
 */
public class RainyImagesGotEvent {

    public ArrayList<String> imageUrls;

    public RainyImagesGotEvent(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public ArrayList<String> getImageUrls() {
        return this.imageUrls;
    }
}

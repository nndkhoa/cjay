package com.cloudjay.cjay.event;

/**
 * Created by nambv on 07/10/2014.
 */
public class ImageCapturedEvent {
    private String imageUrl;

    public ImageCapturedEvent(String imageUrl) {
        this.imageUrl= imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

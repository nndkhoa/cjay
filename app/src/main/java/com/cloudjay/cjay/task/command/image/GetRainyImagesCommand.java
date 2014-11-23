package com.cloudjay.cjay.task.command.image;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.image.RainyImagesGotEvent;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by nambv on 2014/11/23.
 */
public class GetRainyImagesCommand extends Command {

    Context context;

    public GetRainyImagesCommand(Context context) {
        this.context = context;
    }

    @Override
    protected void run() throws SnappydbException {
        DataCenter dataCenter = DataCenter_.getInstance_(context);
        ArrayList<String> imageUrls = dataCenter.getRainyImages(context);
        EventBus.getDefault().post(new RainyImagesGotEvent(imageUrls));
    }
}

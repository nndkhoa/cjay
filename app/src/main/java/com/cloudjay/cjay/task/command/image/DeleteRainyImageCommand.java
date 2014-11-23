package com.cloudjay.cjay.task.command.image;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.image.RainyImagesDeletedEvent;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by nambv on 2014/11/23.
 */
public class DeleteRainyImageCommand extends Command {


    private Context context;
    private ArrayList<String> imageUrls;

    public DeleteRainyImageCommand(Context context, ArrayList<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @Override
    protected void run() throws SnappydbException {
        DataCenter dataCenter = DataCenter_.getInstance_(context);
        dataCenter.deleteRainyImage(context, imageUrls);
        EventBus.getDefault().post(new RainyImagesDeletedEvent());
    }
}

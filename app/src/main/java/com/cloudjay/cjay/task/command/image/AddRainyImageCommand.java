package com.cloudjay.cjay.task.command.image;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

/**
 * Created by nambv on 2014/11/23.
 */
public class AddRainyImageCommand extends Command {

    private Context context;
    private String uuid;
    private String rainyImageUrl;

    public AddRainyImageCommand(Context context, String uuid, String rainyImageUrl) {
        this.context = context;
        this.uuid = uuid;
        this.rainyImageUrl = rainyImageUrl;
    }

    @Override
    protected void run() throws SnappydbException {
        DataCenter dataCenter = DataCenter_.getInstance_(context);
        dataCenter.saveRainyImage(context, uuid, rainyImageUrl);
    }
}

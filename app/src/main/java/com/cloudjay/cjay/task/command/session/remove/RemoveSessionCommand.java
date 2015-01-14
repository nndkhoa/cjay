package com.cloudjay.cjay.task.command.session.remove;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.CJayConstant;
import com.snappydb.SnappydbException;

public class RemoveSessionCommand extends Command {

    Context context;
    String containerId;

    public RemoveSessionCommand(Context context, String containerId) {
        this.context = context;
        this.containerId = containerId;
    }

    @Override
    protected void run() throws SnappydbException {
        DataCenter dataCenter = DataCenter_.getInstance_(context);
        dataCenter.removeSession(context, containerId, null);
    }
}

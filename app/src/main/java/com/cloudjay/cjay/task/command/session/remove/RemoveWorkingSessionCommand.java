package com.cloudjay.cjay.task.command.session.remove;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

public class RemoveWorkingSessionCommand extends Command {
    Context context;
    String containerId;

    public RemoveWorkingSessionCommand(Context context, String containerId) {
        this.context = context;
        this.containerId = containerId;
    }

    @Override
    public void run() {
	    DataCenter dataCenter = DataCenter_.getInstance_(context);
	    dataCenter.removeSession(context, containerId, CJayConstant.PREFIX_WORKING);
    }
}

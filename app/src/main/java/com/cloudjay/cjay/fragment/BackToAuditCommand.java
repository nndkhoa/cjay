package com.cloudjay.cjay.fragment;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

/**
 * Created by nambv on 2015/01/20.
 */
public class BackToAuditCommand extends Command {
    Context context;
    String containerId;

    public BackToAuditCommand(Context context, String containerId) {
        this.context = context;
        this.containerId = containerId;
    }

    @Override
    protected void run() throws SnappydbException {
        DataCenter dataCenter = DataCenter_.getInstance_(context);
        dataCenter.backToAudit(context, containerId);
    }
}

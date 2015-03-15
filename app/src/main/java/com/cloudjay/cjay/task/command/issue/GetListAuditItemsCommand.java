package com.cloudjay.cjay.task.command.issue;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.issue.AuditItemsGotEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;

import de.greenrobot.event.EventBus;

public class GetListAuditItemsCommand extends Command {

    Context context;
    String containerId;

    public GetListAuditItemsCommand(Context context, String containerId) {
        this.context = context;
        this.containerId = containerId;
    }

    @Override
    public void run() {
        DataCenter dataCenter = DataCenter_.getInstance_(context);
        Session session = dataCenter.getSession(context, containerId);
        EventBus.getDefault().post(new AuditItemsGotEvent(session.getAuditItems()));
    }
}

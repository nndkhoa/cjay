package com.cloudjay.cjay.task.command.issue;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.issue.AuditItemGotEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.Logger;

import de.greenrobot.event.EventBus;

public class GetAuditItemCommand extends Command {


    Context context;
    String containerId;
    String itemUuid;

    public GetAuditItemCommand(Context context, String containerId, String itemUuid) {
        this.context = context;
        this.containerId = containerId;
        this.itemUuid = itemUuid;

    }

    @Override
    public void run() {
        Logger.Log("getAuditItemInBackground");
        DataCenter dataCenter = DataCenter_.getInstance_(context);
        Session session = dataCenter.getSession(context, containerId);
        if (session != null) {
            AuditItem auditItem = session.getAuditItem(itemUuid);
            EventBus.getDefault().post(new AuditItemGotEvent(auditItem));
        }
    }
}

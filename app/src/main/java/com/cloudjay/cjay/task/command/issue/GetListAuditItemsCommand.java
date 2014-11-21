package com.cloudjay.cjay.task.command.issue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.event.issue.AuditItemsGotEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

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
        try {
            DB db = App.getDB(context);
            Session session = db.getObject(containerId, Session.class);
            EventBus.getDefault().post(new AuditItemsGotEvent(session.getAuditItems()));
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }
}

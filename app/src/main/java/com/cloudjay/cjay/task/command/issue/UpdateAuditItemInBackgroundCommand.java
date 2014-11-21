package com.cloudjay.cjay.task.command.issue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.event.issue.AuditItemChangedEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.Logger;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

/**
 * Created by nambv on 2014/11/22.
 */
public class UpdateAuditItemInBackgroundCommand extends Command {

    Context context;
    String containerId;
    AuditItem auditItem;

    public UpdateAuditItemInBackgroundCommand(Context context, String containerId, AuditItem auditItem) {
        this.containerId = containerId;
        this.context = context;
        this.auditItem = auditItem;
    }

    @Override
    public void run() {
        Logger.Log("updateAuditItemInBackground");
        try {
            // find session
            DB db = App.getDB(context);
            Session session = db.getObject(containerId, Session.class);
            session.updateAuditItem(auditItem);
            db.put(containerId, session);

            // Notify that an audit item is updated
            EventBus.getDefault().post(new AuditItemChangedEvent(containerId));

        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }
}

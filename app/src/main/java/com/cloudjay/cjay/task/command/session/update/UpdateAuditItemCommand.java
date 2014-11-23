package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.UploadType;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import org.androidannotations.annotations.Bean;

import de.greenrobot.event.EventBus;

public class UpdateAuditItemCommand extends Command {

    private String containerId;
    private Context context;
	private AuditItem result;

    public UpdateAuditItemCommand(Context context, AuditItem result, String containerId) {
        this.context = context;
        this.containerId = containerId;
	    this.result = result;
    }

    @Override
    public void run() {
        DB db;
        String key = containerId;
        Session object = null;
        try {

            db = App.getDB(context);
            object = db.getObject(key, Session.class);
            object.updateAuditItem(result);
	        db.put(key, object);

        } catch (SnappydbException e) {
            Logger.wtf(e.getMessage());
        } finally {
            EventBus.getDefault().post(new UploadSucceededEvent(object, UploadType.AUDIT_ITEM));
        }
    }
}

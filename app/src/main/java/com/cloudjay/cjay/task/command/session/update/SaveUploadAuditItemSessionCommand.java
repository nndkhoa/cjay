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

public class SaveUploadAuditItemSessionCommand extends Command {
    DataCenter dataCenter;

    private String containerId;
    private Context context;
    private UploadType type;
    private AuditItem result;

    public SaveUploadAuditItemSessionCommand(Context context, AuditItem result, UploadType type, String containerId) {
        this.context = context;
        this.containerId = containerId;
        this.type = type;
        this.result = result;
	    this.dataCenter = DataCenter_.getInstance_(context);
    }

    @Override
    public void run() {
        DB db = null;
        String key = containerId;
        Session object = null;
        try {
            db = App.getDB(context);

            object = db.getObject(key, Session.class);
            object.updateAuditItem(result);
//            dataCenter.saveSession(context, object, type);

        } catch (SnappydbException e) {
            Logger.wtf(e.getMessage());
        } finally {
            EventBus.getDefault().post(new UploadSucceededEvent(object, UploadType.SESSION));
        }
    }
}

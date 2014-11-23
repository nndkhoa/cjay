package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

public class SaveSessionCommand extends Command {
    Context context;
    Session session;
    UploadType uploadType;

    public SaveSessionCommand(Context context, Session session, UploadType uploadType) {
        this.context = context;
        this.session = session;
        this.uploadType = uploadType;
    }

    @Override
    public void run() {
        DB db = null;
        String key = session.getContainerId();
        Session object = null;
        try {
            db = App.getDB(context);

            object = db.getObject(key, Session.class);
            Logger.w("From save session: " + session.getModifiedAt());

            object.mergeSession(session);
            object.setUploadStatus(UploadStatus.COMPLETE);

            db.put(key, object);

        } catch (SnappydbException e) {

            // change session to local
            Logger.wtf(e.getMessage());
            try {
                object = session.changeToLocalFormat();
                db.put(key, object);
            } catch (SnappydbException e1) {
                e1.printStackTrace();
            }
        } finally {
            EventBus.getDefault().post(new UploadSucceededEvent(object, UploadType.SESSION));
        }
    }
}

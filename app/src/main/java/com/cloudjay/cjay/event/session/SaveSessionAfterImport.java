package com.cloudjay.cjay.event.session;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.task.job.UploadAuditItemJob;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.Step;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.cloudjay.cjay.util.enums.UploadType;
import com.path.android.jobqueue.JobManager;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

/**
 * Created by nambv on 2014/11/22.
 */
public class SaveSessionAfterImport extends Command {

    Context context;
    Session session;
    UploadType type;

    public SaveSessionAfterImport(Context context, Session session, UploadType type) {
        this.context = context;
        this.session = session;
        this.type = type;
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

            // #tieubao
            // Nếu container vừa được upload
            if (session.getStep() == Step.AUDIT.value) {
                for (AuditItem item : object.getAuditItems()) {
                    if (item.isUploadConfirmed()) {
                        Logger.Log("Add audit item to jobqueue: " + item.toString());
                        JobManager jobManager = App.getJobManager();
                        jobManager.addJobInBackground(new UploadAuditItemJob(session.getId(), item, session.getContainerId(), false));
                    }
                }
            }

            object.mergeSession(session);
            object.setUploadStatus(UploadStatus.COMPLETE);

            Logger.logJson("session from server: ", session, Session.class);

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

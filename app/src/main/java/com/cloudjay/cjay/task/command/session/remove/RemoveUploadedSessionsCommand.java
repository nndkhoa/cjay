package com.cloudjay.cjay.task.command.session.remove;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.event.session.UploadedContainerRemovedEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

public class RemoveUploadedSessionsCommand extends Command {

    Context context;

    public RemoveUploadedSessionsCommand(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        try {

            DB db = App.getDB(context);
            String[] keys = db.findKeys(CJayConstant.PREFIX_UPLOADING);

            for (String key : keys) {

                String t = key.substring(CJayConstant.PREFIX_UPLOADING.length(), key.length());
                Session object = db.getObject(t, Session.class);

                if (object.getUploadStatus() == UploadStatus.COMPLETE.value) {
                    db.del(key);
                    Logger.Log(" > Remove container from upload collection: " + key);
                }
            }

            EventBus.getDefault().post(new UploadedContainerRemovedEvent());
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }
}

package com.cloudjay.cjay.task.command.session.remove;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
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
        DataCenter dataCenter = DataCenter_.getInstance_(context);
        dataCenter.removeUploadedSessions();
        EventBus.getDefault().post(new UploadedContainerRemovedEvent());
    }
}

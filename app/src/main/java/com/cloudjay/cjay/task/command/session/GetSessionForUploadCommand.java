package com.cloudjay.cjay.task.command.session;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.event.session.ContainerForUploadGotEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.Logger;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by nambv on 2014/11/22.
 */
public class GetSessionForUploadCommand implements Command {

    Context context;
    String containerId;

    public GetSessionForUploadCommand(Context context, String containerId) {
        this.context = context;
        this.containerId = containerId;
    }

    @Override
    public void run() {
        try {
            DB db = App.getDB(context);
            String key = containerId;
            Session session = db.getObject(key, Session.class);

            List<Session> list = new ArrayList<>();
            list.add(session);
            EventBus.getDefault().post(new ContainerForUploadGotEvent(list, containerId));

        } catch (SnappydbException e) {
            Logger.w(e.getMessage());
        }
    }
}

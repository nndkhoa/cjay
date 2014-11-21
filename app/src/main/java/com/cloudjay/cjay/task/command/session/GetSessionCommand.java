package com.cloudjay.cjay.task.command.session;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.Logger;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

/**
 * Created by nambv on 2014/11/22.
 */
public class GetSessionCommand implements Command {

    Context context;
    String containerId;

    public GetSessionCommand(Context context, String containerId) {
        this.context = context;
        this.containerId = containerId;
    }

    @Override
    public void run() {
        try {
            DB db = App.getDB(context);
            String key = containerId;
            Session session = db.getObject(key, Session.class);
            EventBus.getDefault().post(new ContainerGotEvent(session, containerId));

        } catch (SnappydbException e) {
            Logger.w(e.getMessage());
        }
    }
}

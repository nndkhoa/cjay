package com.cloudjay.cjay.task.command.session;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.enums.Step;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

/**
 * Created by nambv on 2014/11/22.
 */
public class ChangeSessionLocalStepCommand implements Command {

    Context context;
    String containerId;
    Step step;

    public ChangeSessionLocalStepCommand(Context context, String containerId, Step step) {
        this.context = context;
        this.containerId = containerId;
        this.step = step;
    }

    @Override
    public void run() {
        DB db;
        try {

            db = App.getDB(context);
            Session session = db.getObject(containerId, Session.class);
            session.setLocalStep(step.value);
            db.put(containerId, session);

            EventBus.getDefault().post(new ContainerGotEvent(session, containerId));

        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }
}

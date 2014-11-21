package com.cloudjay.cjay.task.command.session;


import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.event.session.ContainersGotEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class GetListSessionsCommand implements Command {

    String prefix;
    Context context;

    public GetListSessionsCommand(Context context, String prefix) {
        this.context = context;
        this.prefix = prefix;
    }

    @Override
    public void run() {

        int len = prefix.length();

        DB db = null;
        String[] keysResult = new String[0];
        List<Session> sessions = new ArrayList<>();

        try {
            db = App.getDB(context);
            keysResult = db.findKeys(prefix);
        } catch (SnappydbException e) {
            Logger.e(e.getMessage());
        }

        for (String result : keysResult) {

            String newKey = result.substring(len);
            Session session;
            try {
                session = db.getObject(newKey, Session.class);
                sessions.add(session);
            } catch (SnappydbException e) {
                e.printStackTrace();
//                addLog(context, newKey, prefix + " | Cannot retrieve this container", CJayConstant.PREFIX_LOG);
            }
        }

        EventBus.getDefault().post(new ContainersGotEvent(sessions, prefix));
    }

}

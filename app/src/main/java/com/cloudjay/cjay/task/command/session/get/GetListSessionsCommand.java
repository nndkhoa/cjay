package com.cloudjay.cjay.task.command.session.get;


import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
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

public class GetListSessionsCommand extends Command {

    String prefix;
    Context context;

    public GetListSessionsCommand(Context context, String prefix) {
        this.context = context;
        this.prefix = prefix;
    }

    @Override
    public void run() {
	    DataCenter dataCenter = DataCenter_.getInstance_(context);
	    List<Session> sessions = dataCenter.getListSessions(context, prefix);

        EventBus.getDefault().post(new ContainersGotEvent(sessions, prefix));
    }

}

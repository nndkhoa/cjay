package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;

public class UpdateImportSessionCommand extends Command {

    Context context;
    Session session;

    public UpdateImportSessionCommand(Context context, Session session) {
        this.context = context;
        this.session = session;
    }

    @Override
    public void run() {
	    DataCenter dataCenter = DataCenter_.getInstance_(context);
	    dataCenter.updateImportSession(context, session);
    }
}

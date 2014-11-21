package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;

/**
 * Created by nambv on 2014/11/22.
 */
public class UpdateImportSessionCommand extends Command {

    Context context;
    Session session;

    public UpdateImportSessionCommand(Context context, Session session) {
        this.context = context;
        this.session = session;
    }

    @Override
    public void run() {

    }
}

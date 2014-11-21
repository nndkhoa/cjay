package com.cloudjay.cjay.task.command.session;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

public class RemoveWorkingSessionCommand extends Command {
    Context context;
    String containerId;

    public RemoveWorkingSessionCommand(Context context, String containerId) {
        this.context = context;
        this.containerId = containerId;
    }

    @Override
    public void run() {
        try {
            DB db = App.getDB(context);

            String workingKey = CJayConstant.PREFIX_WORKING + containerId;
            db.del(workingKey);

            Logger.Log("REMOVE container " + containerId + " from Working collection");

        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }
}

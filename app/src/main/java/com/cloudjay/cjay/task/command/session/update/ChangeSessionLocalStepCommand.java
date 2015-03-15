package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.enums.Step;

/**
 * Change container session local step in background
 */
public class ChangeSessionLocalStepCommand extends Command {

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
        DataCenter dataCenter = DataCenter_.getInstance_(context);
        dataCenter.changeSessionLocalStepInBackground(context, containerId, step);
    }
}

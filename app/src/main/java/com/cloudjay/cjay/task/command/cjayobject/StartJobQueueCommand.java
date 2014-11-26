package com.cloudjay.cjay.task.command.cjayobject;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

/**
 * Created by thai on 26/11/2014.
 */
public class StartJobQueueCommand extends Command {
	Context context;

	public StartJobQueueCommand(Context context) {
		this.context = context;
	}

	@Override
	protected void run() throws SnappydbException {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.startJobQueue(context);
	}
}

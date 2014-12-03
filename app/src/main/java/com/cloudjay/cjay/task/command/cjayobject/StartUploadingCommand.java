package com.cloudjay.cjay.task.command.cjayobject;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

public class StartUploadingCommand extends Command {

	Context context;
	public StartUploadingCommand(Context context) {
		this.context = context;
	}

	@Override
	protected void run() throws SnappydbException {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.startJobQueue(context);
	}
}

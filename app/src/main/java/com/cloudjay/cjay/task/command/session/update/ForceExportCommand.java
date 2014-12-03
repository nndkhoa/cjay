package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.task.command.Command;

public class ForceExportCommand extends Command {
	Context context;
	String containerId;

	public ForceExportCommand(Context context, String containerId) {
		this.context = context;
		this.containerId = containerId;
	}

	@Override
	public void run() {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.forceExport(context, containerId);
	}
}

package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.session.ContainerGotEvent;
import com.cloudjay.cjay.event.upload.UploadSucceededEvent;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.enums.UploadType;

import de.greenrobot.event.EventBus;

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

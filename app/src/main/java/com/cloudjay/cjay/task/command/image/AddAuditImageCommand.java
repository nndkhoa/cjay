package com.cloudjay.cjay.task.command.image;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.task.command.Command;

public class AddAuditImageCommand extends Command {

	Context context;
	AuditImage image;
	String containerId;

	public AddAuditImageCommand(Context context, AuditImage image, String containerId) {
		this.containerId = containerId;
		this.context = context;
		this.image = image;
	}

	@Override
	protected void run() {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.addAuditImage(context, image, containerId);
	}
}

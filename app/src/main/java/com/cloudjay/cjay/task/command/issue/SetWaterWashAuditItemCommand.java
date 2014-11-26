package com.cloudjay.cjay.task.command.issue;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.issue.AuditItemChangedEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

public class SetWaterWashAuditItemCommand extends Command {

	Context context;
	String containerId;
	AuditItem auditItem;

	public SetWaterWashAuditItemCommand(Context context, final AuditItem auditItem, String containerId) {
		this.context = context;
		this.containerId = containerId;
		this.auditItem = auditItem;
	}

	@Override
	protected void run() throws SnappydbException {

		DataCenter dataCenter = DataCenter_.getInstance_(context);
		boolean success = dataCenter.setWaterWashType(context, auditItem, containerId);
		
		if (success)
			EventBus.getDefault().post(new AuditItemChangedEvent(containerId));
	}
}

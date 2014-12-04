package com.cloudjay.cjay.task.command.issue;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.issue.AuditItemChangedEvent;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

public class RemoveAuditItemCommand extends Command {

	Context context;
	String containerId;
	String itemUuid;

	public RemoveAuditItemCommand(Context context, String containerId, String auditItemUUID) {
		this.context = context;
		this.containerId = containerId;
		this.itemUuid = auditItemUUID;
	}

	@Override
	protected void run() throws SnappydbException {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		boolean success = dataCenter.removeAuditItem(context, containerId, itemUuid);
		
		if (success)
			EventBus.getDefault().post(new AuditItemChangedEvent(containerId));
	}
}

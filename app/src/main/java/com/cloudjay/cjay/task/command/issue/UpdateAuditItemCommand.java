package com.cloudjay.cjay.task.command.issue;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.issue.AuditItemChangedEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.Logger;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

public class UpdateAuditItemCommand extends Command {
    Context context;
    String containerId;
    AuditItem auditItem;

    public UpdateAuditItemCommand(Context context, String containerId, AuditItem auditItem) {
        this.containerId = containerId;
        this.context = context;
        this.auditItem = auditItem;
    }

    @Override
    public void run() {
	    DataCenter dataCenter = DataCenter_.getInstance_(context);
	    boolean success = dataCenter.updateAuditItem(context, containerId, auditItem);

	    // Notify that an audit item is updated
	    if (success) {
		    EventBus.getDefault().post(new AuditItemChangedEvent(containerId));
	    }
    }
}

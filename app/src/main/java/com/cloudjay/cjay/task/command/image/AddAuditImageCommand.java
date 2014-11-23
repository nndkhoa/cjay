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
    int type;

    String auditItenUUID;
    String auditImageUUID;
    String auditItemRemove;

    public AddAuditImageCommand(Context context, AuditImage image, String containerId, int type) {
		this.containerId = containerId;
		this.context = context;
		this.image = image;
        this.type = type;
	}

    public AddAuditImageCommand(Context context,
                                String containerId,
                                String auditItemUUID,
                                String auditItemRemove,
                                String auditImageUUID,
                                int type) {
        this.context = context;
        this.containerId = containerId;
        this.auditItenUUID = auditItemUUID;
        this.auditItemRemove = auditItemRemove;
        this.auditImageUUID = auditImageUUID;
        this.type = type;
    }

	@Override
	protected void run() {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
        switch (type) {
            case 0:
                dataCenter.addAuditImage(context, image, containerId);
                break;
            default:
                dataCenter.addAuditImageToAuditedItem(context, containerId, auditItenUUID,
                        auditItemRemove, auditImageUUID);
        }

	}
}

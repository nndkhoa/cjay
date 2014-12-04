package com.cloudjay.cjay.task.command.image;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.ImageType;

public class AddOrUpdateAuditImageCommand extends Command {

	Context context;
	AuditImage image;
	String containerId;
	String itemUuid;

	public AddOrUpdateAuditImageCommand(Context context, AuditImage image, String containerId, String itemUuid) {
		this.itemUuid = itemUuid;
		this.containerId = containerId;
		this.context = context;
		this.image = image;
	}

	@Override
	protected void run() {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		AuditItem auditItem = dataCenter.getAuditItem(context, containerId, itemUuid);

		// Create temporary audit item
		if (null == auditItem) {
			Logger.Log("Create new Audit Item: " + image.getType());
			dataCenter.addAuditImage(context, image, containerId);
		} else {
			auditItem.getAuditImages().add(image);
			if (image.getType() == ImageType.REPAIRED.value) {
				auditItem.setRepaired(true);
			}
			dataCenter.updateAuditItem(context, containerId, auditItem);
		}
	}
}

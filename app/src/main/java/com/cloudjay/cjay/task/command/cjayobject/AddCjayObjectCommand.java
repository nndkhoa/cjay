package com.cloudjay.cjay.task.command.cjayobject;

import android.content.Context;
import android.content.Intent;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.model.CJayObject;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.task.service.UploadIntentService_;
import com.snappydb.SnappydbException;

public class AddCJayObjectCommand extends Command {

	Context context;
	CJayObject object;

	public AddCJayObjectCommand(Context context, CJayObject object) {
		this.context = context;
		this.object = object;
	}

	@Override
	protected void run() throws SnappydbException {

		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.addCJayObject(object.getContainerId(), object);
		context.startService(new Intent(context, UploadIntentService_.class));
		// --> thread will end here
	}
}

package com.cloudjay.cjay.task.command.cjayobject;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadObjectRemovedEvent;
import com.cloudjay.cjay.model.UploadObject;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

import de.greenrobot.event.EventBus;

public class RemoveUploadObjectCommand extends Command {

	Context context;

	public RemoveUploadObjectCommand(Context context) {
		this.context = context;
	}

	@Override
	protected void run() throws SnappydbException {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.remove(context);
		EventBus.getDefault().post(new UploadObjectRemovedEvent());
	}
}

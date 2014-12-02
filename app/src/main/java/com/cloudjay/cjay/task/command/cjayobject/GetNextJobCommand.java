package com.cloudjay.cjay.task.command.cjayobject;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.model.CJayObject;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

public class GetNextJobCommand extends Command {
	CJayObject object;
	Context context;

	public GetNextJobCommand(Context context, CJayObject object) {
		this.context = context;
		this.object = object;
	}

	@Override
	protected void run() throws SnappydbException {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.getNextCJayObject(object.getContainerId(), object);
	}
}

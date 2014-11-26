package com.cloudjay.cjay.task.command.cjayobject;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.model.CJayObject;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.Logger;
import com.snappydb.SnappydbException;

/**
 * Created by thai on 26/11/2014.
 */
public class AddCjayObjectCommand extends Command {

	Context context;
	CJayObject object;

	public AddCjayObjectCommand(Context context, CJayObject object) {
		this.context = context;
		this.object = object;
	}

	@Override
	protected void run() throws SnappydbException {
		Logger.e("ADD CJAY TO COMMAND");
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.addCJayObject(object.getContainerId(), object);
	}
}

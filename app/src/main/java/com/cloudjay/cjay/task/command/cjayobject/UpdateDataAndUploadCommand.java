package com.cloudjay.cjay.task.command.cjayobject;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.CJayObject;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.SnappydbException;

/**
 * Created by thai on 27/11/2014.
 */
public class UpdateDataAndUploadCommand extends Command {
	Context context;
	CJayObject object;
	public UpdateDataAndUploadCommand(CJayObject object) {
		this.object = object;
	}

	@Override
	protected void run() throws SnappydbException {

		context = App.getInstance().getApplicationContext();

		DataCenter dataCenter = DataCenter_.getInstance_(context);

		CJayObject cJayObject = dataCenter.updateCjayOject(object);

		dataCenter.addCJayToJob(cJayObject);

	}
}

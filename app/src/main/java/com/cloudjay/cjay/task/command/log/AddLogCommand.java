package com.cloudjay.cjay.task.command.log;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.cloudjay.cjay.util.CJayConstant;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

/**
 * Add container to database
 */
public class AddLogCommand extends Command {

	Context context;
	String title;
	String message;
	String logPrefix;

	public AddLogCommand(Context context, String title, String message, String logPrefix) {
		this.context = context;
		this.title = title;
		this.message = message;
		this.logPrefix = logPrefix;
	}

	@Override
	protected void run() {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.addLog(context, title, message, logPrefix);
	}
}

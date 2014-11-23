package com.cloudjay.cjay.task.command.session.update;

import android.content.Context;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;

/**
 * Add container to database
 */
public class AddSessionCommand extends Command {

	Context context;
	Session session;

	public AddSessionCommand(Context context, Session session) {
		this.context = context;
		this.session = session;
	}

	@Override
	protected void run() {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.addOrUpdateSession(context, session);
	}
}
